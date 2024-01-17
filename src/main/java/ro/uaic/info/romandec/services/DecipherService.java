package ro.uaic.info.romandec.services;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import ro.uaic.info.romandec.exceptions.HttpClientException;
import ro.uaic.info.romandec.exceptions.ManuscriptNotFoundException;
import ro.uaic.info.romandec.models.ThirdPartyManuscript;
import ro.uaic.info.romandec.models.dtos.NLPStatusIdDto;
import ro.uaic.info.romandec.models.dtos.OCRStatusIdDto;
import ro.uaic.info.romandec.models.dtos.ThirdPartyManuscriptDto;
import ro.uaic.info.romandec.repository.ThirdPartyManuscriptRepository;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpStatus.*;

@Service
public class DecipherService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DecipherService.class);
    private static final String LOGIN_ERROR = "Unable to login to third party application";
    private static final String NOT_FOUND_ERROR = "No such manuscript for given docId";
    private static final String BASE_URL_API = "http://127.0.0.1:8001/api/v1";

    private final WebClient webClient;

    private final ThirdPartyManuscriptRepository thirdPartyManuscriptRepository;
    private final ModelMapper mapper;
    @Autowired
    public DecipherService(ThirdPartyManuscriptRepository thirdPartyManuscriptRepository, ModelMapper modelMapper) {
        this.thirdPartyManuscriptRepository = thirdPartyManuscriptRepository;
        this.mapper = modelMapper;
        this.webClient = WebClient.create(BASE_URL_API);
    }

    public boolean isAlive() {
        var status = Objects.requireNonNull(webClient.get().uri(BASE_URL_API + "/health")
                        .retrieve()
                        .toBodilessEntity()
                        .block())
                .getStatusCode();
        return status == OK;
    }

    public boolean login() {
        //    @Value("${module.third-party-uri}")
        String thirdPartyLoginUri = "http://127.0.0.1:8001/api/v1/third-party/login";
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
        var postUri = BASE_URL_API + "/upload_manuscript";

        LOGGER.info("Trying to upload file to {}", postUri);

        var response = webClient.post()
                .uri(URI.create(postUri))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBuilder.build()))
                .retrieve()
                .bodyToMono(ThirdPartyManuscriptDto.class)
                .block();

        if (response == null) {
            LOGGER.error("Unable to upload file");
            throw new HttpClientException("Unable to perform the upload");
        }
        thirdPartyManuscriptRepository.save(mapper.map(response, ThirdPartyManuscript.class));
        LOGGER.info("File {} uploaded successfully", response.getFilename());
        return response;
    }

    @SneakyThrows
    public Long sendToOcrDocument(@NonNull ThirdPartyManuscriptDto manuscript) {
        if(!login()){
            throw new HttpClientException(LOGIN_ERROR);
        }

        String upload_status = "";
        while(!upload_status.equals("\"FINISHED\"")) {
            upload_status =  webClient.get()
                    .uri(URI.create(String.format("%s/job_status/%s", BASE_URL_API, manuscript.getJobId())))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            LOGGER.info("Document creation status: {}", upload_status);
            TimeUnit.MILLISECONDS.sleep(333);
        }

        LOGGER.info("Sending manuscript to decipher on 3rd party");
        var response = webClient.post()
                .uri(URI.create(String.format(BASE_URL_API + "/ocr/send_to_recognition?filename=%s&document_id=%s",
                        manuscript.getFilename(), manuscript.getDocId())))
                .retrieve()
                .bodyToMono(OCRStatusIdDto.class)
                .block();

        assert response != null;

        var dbManuscript = thirdPartyManuscriptRepository.findThirdPartyManuscriptByDocId(manuscript.getDocId())
                .orElseThrow(() -> new ManuscriptNotFoundException(NOT_FOUND_ERROR));

        try {
            dbManuscript.setOcrJobId(Long.parseLong(response.getOcr_job_id()));
            thirdPartyManuscriptRepository.save(dbManuscript);

            LOGGER.info("OCR job id {}", response.getOcr_job_id());
            return Long.parseLong(response.getOcr_job_id());
        } catch (RuntimeException e) {
            throw new HttpClientException(e.getMessage());
        }
    }

    public String getDecipheringStatus(Long jobId) {
        if(!login()){
            throw new HttpClientException(LOGIN_ERROR);
        }

        // get text recognition by manuscript text_recon_job_id
        var response = webClient.get()
                .uri(URI.create(String.format("%s/job_status/%s", BASE_URL_API, jobId)))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        LOGGER.info("Job with id {} has status {}", jobId, response);
        return response;
    }

    @SneakyThrows
    public String getOcrText(Long docId) {
        var manuscript = thirdPartyManuscriptRepository.findThirdPartyManuscriptByDocId(docId)
                .orElseThrow(() -> new ManuscriptNotFoundException(NOT_FOUND_ERROR));

        var ocrJobId = this.sendToOcrDocument(mapper.map(manuscript, ThirdPartyManuscriptDto.class));

        if(!login()) {
            throw new HttpClientException(LOGIN_ERROR);
        }

        String ocr_status = "";
        while(!ocr_status.equals("\"FINISHED\"")) {
            ocr_status = getDecipheringStatus(ocrJobId);
            LOGGER.info("Document OCR status: {}", ocr_status);
            TimeUnit.MILLISECONDS.sleep(333);
        }

        LOGGER.info("Getting OCR text for docId {}", docId);
        return webClient.get()
                .uri(URI.create(String.format("%s/ocr/get_translated_xml_text?filename=%s&document_id=%s",
                        BASE_URL_API, manuscript.getFilename(), manuscript.getDocId().toString())))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    @SneakyThrows
    public Long sendToNLPDocument(@NonNull ThirdPartyManuscriptDto manuscript) {
        if(!login()) {
            throw new HttpClientException(LOGIN_ERROR);
        }

        String upload_status = "";
        while(!upload_status.equals("\"FINISHED\"")) {
            upload_status =  webClient.get()
                    .uri(URI.create(String.format("%s/job_status/%s", BASE_URL_API, manuscript.getJobId())))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            LOGGER.info("Document creation status: {}", upload_status);
            TimeUnit.MILLISECONDS.sleep(333);
        }

        LOGGER.info("Sending manuscript to NLP on 3rd party");
        var response = webClient.post()
                .uri(URI.create(String.format(BASE_URL_API + "/nlp/send_to_recognition?filename=%s&document_id=%s",
                        manuscript.getFilename(), manuscript.getDocId())))
                .retrieve()
                .bodyToMono(NLPStatusIdDto.class)
                .block();

        assert response != null;

        var dbManuscript = thirdPartyManuscriptRepository.findThirdPartyManuscriptByDocId(manuscript.getDocId())
                .orElseThrow(() -> new ManuscriptNotFoundException(NOT_FOUND_ERROR));
        try {
            dbManuscript.setNlpJobId(Long.parseLong(response.getNlp_job_id()));
            thirdPartyManuscriptRepository.save(dbManuscript);

            LOGGER.info("NLP job id {}", response.getNlp_job_id());
            return Long.parseLong(response.getNlp_job_id());
        } catch (RuntimeException e) {
            throw new HttpClientException(e.getMessage());
        }
    }

    @SneakyThrows
    public Object getNlpText(Long docId) {
        var manuscript = thirdPartyManuscriptRepository.findThirdPartyManuscriptByDocId(docId)
                .orElseThrow(() -> new ManuscriptNotFoundException(NOT_FOUND_ERROR));
        if(!login()) {
            throw new HttpClientException(LOGIN_ERROR);
        }

        var nlpJobId = this.sendToNLPDocument(mapper.map(manuscript, ThirdPartyManuscriptDto.class));

        String nlp_status = "";
        while(!nlp_status.equals("\"FINISHED\"")) {
            nlp_status = getDecipheringStatus(nlpJobId);
            LOGGER.info("Document NLP status: {}", nlp_status);
            TimeUnit.MILLISECONDS.sleep(333);
        }

        LOGGER.info("Getting NLP text for docId {}", docId);
        return webClient.get()
                .uri(URI.create(String.format("%s/nlp/get_translated_xml_text?filename=%s&document_id=%s",
                        BASE_URL_API, manuscript.getFilename(), manuscript.getDocId().toString())))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
