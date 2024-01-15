package ro.uaic.info.romandec.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ro.uaic.info.romandec.models.dtos.ManuscriptPreviewResponseDto;
import ro.uaic.info.romandec.services.ManuscriptService;

import java.util.UUID;

@RestController
@RequestMapping("/api/manuscript")
public class ManuscriptController {

    private final ManuscriptService manuscriptService;

    @Autowired
    public ManuscriptController(ManuscriptService manuscriptService) {
        this.manuscriptService = manuscriptService;
    }

    @PostMapping("/decipher")
    public ResponseEntity<?> decipherManuscript(@RequestParam("manuscript") MultipartFile manuscript,
                                                @RequestParam("manuscriptDetails") String decipherManuscriptJSON) {

        //replace this with method for extracting user id from jwt;
        UUID userId = UUID.fromString("77872158-dafb-4b4d-aa56-0ea99c5bf91f");

        ManuscriptPreviewResponseDto response = manuscriptService.decipherTranscript(manuscript, decipherManuscriptJSON, userId);

        if (response == null) {
            return new ResponseEntity<>("Failed at deciphering.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
