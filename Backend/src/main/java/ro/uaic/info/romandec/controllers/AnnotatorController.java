package ro.uaic.info.romandec.controllers;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.uaic.info.romandec.exceptions.NoAvailableImageForAnnotator;
import ro.uaic.info.romandec.services.ManuscriptService;

import java.io.*;
import java.util.List;

@RestController
@RequestMapping("/api/annotator")
public class AnnotatorController {

    private final ManuscriptService manuscriptService;

    public AnnotatorController(ManuscriptService manuscriptService)
    {
        this.manuscriptService = manuscriptService;
    }

    @GetMapping
    public ResponseEntity<?> getRandomNotDecipheredImage()
    {
        ResponseEntity<?> response;
        try
        {
            File file = manuscriptService.getRandomNotDecipheredImage();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentDispositionFormData("inline", file.getName());
            headers.setAccessControlExposeHeaders(List.of("Content-Disposition"));

            InputStream in = new FileInputStream(file);

            response = ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(headers)
                    .body(new InputStreamResource(in));

        }
        catch (NoAvailableImageForAnnotator | IOException e)
        {
            response = ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }

        return response;
    }
}
