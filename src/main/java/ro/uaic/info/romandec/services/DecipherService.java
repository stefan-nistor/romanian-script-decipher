package ro.uaic.info.romandec.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import ro.uaic.info.romandec.exceptions.HttpClientException;
import ro.uaic.info.romandec.exceptions.ManuscriptNotFoundException;
import ro.uaic.info.romandec.models.ThirdPartyManuscript;
import ro.uaic.info.romandec.models.dtos.JobStatusIdDto;
import ro.uaic.info.romandec.models.dtos.ThirdPartyManuscriptDto;
import ro.uaic.info.romandec.repository.ThirdPartyManuscriptRepository;

import java.net.URI;
import java.util.Objects;

import static org.springframework.http.HttpStatus.*;

@Service
public class DecipherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecipherService.class);
    private static final String LOGIN_ERROR = "Unable to login to third party application";
    private static final String NOT_FOUND_ERROR = "No such manuscript for given docId";

//    @Value("${module.ai.baseUri.api}")
    private static String baseUriApi = "http://127.0.0.1:8001/api/v1";

//    @Value("${module.third-party-uri}")
    private static String thirdPartyLoginUri = "http://127.0.0.1:8001/api/v1/third-party/login";

    private final WebClient webClient;

    private final ThirdPartyManuscriptRepository thirdPartyManuscriptRepository;
    private final ModelMapper mapper;
    @Autowired
    public DecipherService(ThirdPartyManuscriptRepository thirdPartyManuscriptRepository, ModelMapper modelMapper) {
        this.thirdPartyManuscriptRepository = thirdPartyManuscriptRepository;
        this.mapper = modelMapper;
        this.webClient = WebClient.create(baseUriApi);
    }

    public boolean isAlive() {
        var status = Objects.requireNonNull(webClient.get().uri(baseUriApi + "/health")
                        .retrieve()
                        .toBodilessEntity()
                        .block())
                .getStatusCode();
        return status == OK;
    }

    public boolean login() {
        return Objects.requireNonNull(webClient.get().uri(URI.create(thirdPartyLoginUri))
                        .retrieve()
                        .toBodilessEntity()
                        .block())
                .getStatusCode() == OK;
    }

    public ThirdPartyManuscriptDto uploadFile(@NonNull MultipartFile multipartFile) {
        if(!login()){
            throw new HttpClientException(LOGIN_ERROR);
        }

        var multipartBuilder = new MultipartBodyBuilder();
        multipartBuilder.part("file", multipartFile.getResource());
        var postUri = baseUriApi + "/upload_manuscript";

        LOGGER.info("Trying to upload file to {}", postUri);

        var response = webClient.post()
                .uri(URI.create(postUri))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBuilder.build()))
                .retrieve()
                .bodyToMono(ThirdPartyManuscriptDto.class)
                .block();

        if (response == null) {
            throw new HttpClientException("Unable to perform the upload");
        }
        thirdPartyManuscriptRepository.save(mapper.map(response, ThirdPartyManuscript.class));
        return response;
    }

    public Long sendToDecipherDocument(@NonNull ThirdPartyManuscriptDto manuscript) {
        if(!login()){
            throw new HttpClientException(LOGIN_ERROR);
        }

        LOGGER.info("Sending manuscript to decipher on 3rd party");
        var response = webClient.post()
                .uri(URI.create(String.format(baseUriApi + "/ocr/send_to_recognition?filename=%s&document_id=%s",
                        manuscript.getFilename(), manuscript.getDocId())))
                .retrieve()
                .bodyToMono(JobStatusIdDto.class)
                .block();

        assert response != null;

        var dbManuscript = thirdPartyManuscriptRepository.findThirdPartyManuscriptByDocId(manuscript.getDocId())
                .orElseThrow(() -> new ManuscriptNotFoundException(NOT_FOUND_ERROR));

        try {
            dbManuscript.setTextRecognitionJobId(Long.parseLong(response.getText_recognition_job_id()));
            thirdPartyManuscriptRepository.save(dbManuscript);

            LOGGER.info("Deciphering job id {}", response.getText_recognition_job_id());
            return Long.parseLong(response.getText_recognition_job_id());
        } catch (RuntimeException e) {
            throw new HttpClientException(e.getMessage());
        }
    }

    public String getDecipheringStatus(Long docId) {
        if(!login()){
            throw new HttpClientException(LOGIN_ERROR);
        }
        LOGGER.info("Getting deciphering status for docId {}", docId);

        var dbManuscript = thirdPartyManuscriptRepository.findThirdPartyManuscriptByDocId(docId)
                .orElseThrow(() -> new ManuscriptNotFoundException(NOT_FOUND_ERROR));

        // get text recognition by manuscript text_recon_job_id
        var response = webClient.get()
                .uri(URI.create(String.format("%s/job_status/%s", baseUriApi, dbManuscript.getTextRecognitionJobId())))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        LOGGER.info("Job with id {} has status {}", dbManuscript.getTextRecognitionJobId(), response);

        return response;
    }

    public String getTranslatedText(Long docId) {
        var manuscript = thirdPartyManuscriptRepository.findThirdPartyManuscriptByDocId(docId)
                .orElseThrow(() -> new ManuscriptNotFoundException(NOT_FOUND_ERROR));

        if(!login()) {
            throw new HttpClientException(LOGIN_ERROR);
        }

        return webClient.get()
                .uri(URI.create(String.format("%s/ocr/get_translated_xml_text?filename=%s&document_id=%s",
                                baseUriApi, manuscript.getFilename(), manuscript.getDocId().toString())))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
