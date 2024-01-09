package ro.uaic.info.romandec.controllers;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<?> getRandomNotDecipheredImage() throws FileNotFoundException {
        ResponseEntity<?> response;

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


        return response;
    }

//    @PostMapping
//    public ResponseEntity<?> saveAnnotatorDecipheredManuscript(
//            @RequestParam("originalImageFilename") String originalImageFilename,
//            @RequestParam("decipheredText") String decipheredText) throws IOException {
//        ResponseEntity<?> response;
//
//        manuscriptService.saveAnnotatorDecipheredManuscript(originalImageFilename, decipheredText);
//
//        response = ResponseEntity
//                .status(HttpStatus.OK)
//                .body("");
//        return response;
//    }

}
