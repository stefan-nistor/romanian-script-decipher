package ro.uaic.info.romandec.services;

import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import ro.uaic.info.romandec.exceptions.HttpClientException;

import java.util.Map;
import java.util.Objects;

@Service
public class DecipherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecipherService.class);

    @Value("${module.ai.baseUri}")
    private static String baseUri;
    private final WebClient webClient;


    public DecipherService() {
        this.webClient = WebClient.create(baseUri);
    }

    public boolean isAlive() {
        var status = Objects.requireNonNull(webClient.get().uri(baseUri + "/health")
                        .retrieve()
                        .toBodilessEntity()
                        .block())
                .getStatusCode();
        return status == HttpStatus.OK;
    }

    public HttpStatusCode uploadFile(@NonNull final String endpoint, @NonNull MultipartFile multipartFile,
                                     Map<String, String> queryParams) {
        var multipartBuilder = new MultipartBodyBuilder();
        multipartBuilder.part("file", multipartFile.getResource());
        var postUriBuilder = new StringBuilder(baseUri).append(endpoint);

        if (queryParams != null && !queryParams.isEmpty()) {
            postUriBuilder.append("?");
            queryParams.forEach((key, value) -> postUriBuilder.append(key).append("=").append(value).append("&"));
            postUriBuilder.deleteCharAt(postUriBuilder.length() - 1); // Remove the trailing "&"
        }
        var postUri = postUriBuilder.toString();

        LOGGER.info("Trying uploading file to {}", postUri);
        var response = webClient.post().uri(postUri)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBuilder.build()))
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().is2xxSuccessful()) {
                        LOGGER.info("File uploaded successfully");
                        return clientResponse.toBodilessEntity().thenReturn(clientResponse.statusCode());
                    } else {
                        throw new HttpClientException("Unable to perform. Reason: " + clientResponse.statusCode());
                    }
                });
        return response.block();
    }

}
