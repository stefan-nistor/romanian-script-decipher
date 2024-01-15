package ro.uaic.info.romandec.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.uaic.info.romandec.exceptions.InvalidDataException;
import ro.uaic.info.romandec.exceptions.NoAvailableDataForGivenInputException;
import ro.uaic.info.romandec.models.dtos.ManuscriptDetailedResponseDto;
import ro.uaic.info.romandec.models.dtos.ManuscriptPreviewResponseDto;
import ro.uaic.info.romandec.services.ManuscriptService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ManuscriptService manuscriptService;

    private static final UUID USER_ID = UUID.fromString("b6f22768-e6d6-43e3-af3f-ee52891d69dc");
    @Autowired
    public ProfileController(ManuscriptService manuscriptService) {
        this.manuscriptService = manuscriptService;
    }

    @GetMapping("/my-manuscripts/all")
    public ResponseEntity<?> getAllUsersManuscripts() throws NoAvailableDataForGivenInputException {

        //replace this with method for extracting user id from jwt;
        List<ManuscriptPreviewResponseDto> allUsersManuscripts = manuscriptService.getAllUsersManuscripts(USER_ID);

        if (allUsersManuscripts.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("This user has no manuscripts added.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(allUsersManuscripts);
    }

    @GetMapping("/my-manuscripts/specific/{manuscriptId}")
    public ResponseEntity<?> getSpecificManuscript(@PathVariable UUID manuscriptId)
            throws NoAvailableDataForGivenInputException, InvalidDataException {
        ManuscriptDetailedResponseDto manuscript = manuscriptService.getSpecificManuscript(manuscriptId, USER_ID);

        return ResponseEntity.status(HttpStatus.OK).body(manuscript);
    }

    @DeleteMapping("/my-manuscripts/delete/{manuscriptId}")
    public ResponseEntity<?> deleteManuscript(@PathVariable UUID manuscriptId)
            throws NoAvailableDataForGivenInputException, InvalidDataException {
        manuscriptService.deleteManuscript(manuscriptId, USER_ID);

        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    @GetMapping("/my-manuscripts/download-original/{manuscriptId}")
    public ResponseEntity<?> downloadSpecificManuscript(@PathVariable UUID manuscriptId)
            throws NoAvailableDataForGivenInputException, InvalidDataException, IOException {
        FileSystemResource manuscript =  manuscriptService.downloadOriginalManuscript(manuscriptId, USER_ID);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .contentLength(manuscript.contentLength())
                .body(manuscript);
    }
}
