package ro.uaic.info.romandec.controllers;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ro.uaic.info.romandec.models.dtos.ManuscriptPreviewResponseDto;
import ro.uaic.info.romandec.services.DecipherService;
import ro.uaic.info.romandec.services.ManuscriptService;

import java.util.UUID;

@RestController
@RequestMapping("/api/manuscript")
public class ManuscriptController {

    private static final String STATUS_FINISHED = "FINISHED";
    private final ManuscriptService manuscriptService;
    private final DecipherService decipherService;

    @Autowired
    public ManuscriptController(ManuscriptService manuscriptService, DecipherService decipherService) {
        this.manuscriptService = manuscriptService;
        this.decipherService = decipherService;
    }

    @PostMapping("/decipher")
    public ResponseEntity<Object> decipherManuscript(@RequestParam("manuscript") MultipartFile manuscript,
                                                @RequestParam("manuscriptDetails") String decipherManuscriptJSON) {

        // replace this with method for extracting user id from jwt
        UUID userId = UUID.fromString("b6f22768-e6d6-43e3-af3f-ee52891d69dc");

        ManuscriptPreviewResponseDto response = manuscriptService.decipherTranscript(manuscript, decipherManuscriptJSON, userId);
        
        if (response == null) {
            return new ResponseEntity<>("Failed at deciphering.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/decipher-translation/{docId}")
    public ResponseEntity<Object> decipherTranslation(@PathVariable Long docId) {
        var status = decipherService.getDecipheringStatus(docId);
        if(!status.equals(STATUS_FINISHED)){
            return new ResponseEntity<>("", HttpStatus.NO_CONTENT);
        }
        var text = decipherService.getTranslatedText(docId);
        return new ResponseEntity<>(text, HttpStatus.OK);
    }
}
