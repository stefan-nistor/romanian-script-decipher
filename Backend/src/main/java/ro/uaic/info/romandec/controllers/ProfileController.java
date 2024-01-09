package ro.uaic.info.romandec.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.uaic.info.romandec.models.dtos.SpecificManuscriptDto;
import ro.uaic.info.romandec.models.dtos.ManuscriptDetailedResponseDto;
import ro.uaic.info.romandec.models.dtos.ManuscriptPreviewResponseDto;
import ro.uaic.info.romandec.exceptions.InvalidDataException;
import ro.uaic.info.romandec.exceptions.NoAvailableDataForGivenInputException;
import ro.uaic.info.romandec.services.ManuscriptService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ManuscriptService manuscriptService;
    @Autowired
    public ProfileController(ManuscriptService manuscriptService) {
        this.manuscriptService = manuscriptService;
    }

    @GetMapping("/my-manuscripts/all")
    public ResponseEntity<?> getAllUsersManuscripts() throws NoAvailableDataForGivenInputException {

        //replace this with method for extracting user id from jwt;
        UUID userId = UUID.fromString("77872158-dafb-4b4d-aa56-0ea99c5bf91f");

        List<ManuscriptPreviewResponseDto> allUsersManuscripts = manuscriptService.getAllUsersManuscripts(userId);

        if (allUsersManuscripts.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("This user has no manuscripts added.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(allUsersManuscripts);
    }

    @GetMapping("/my-manuscripts/specific")
    public ResponseEntity<?> getSpecificManuscript(@RequestBody SpecificManuscriptDto request)
            throws NoAvailableDataForGivenInputException, InvalidDataException {

        //replace this with method for extracting user id from jwt;
        UUID userId = UUID.fromString("77872158-dafb-4b4d-aa56-0ea99c5bf91f");
        ManuscriptDetailedResponseDto manuscript = manuscriptService.getSpecificManuscript(request, userId);

        return ResponseEntity.status(HttpStatus.OK).body(manuscript);
    }

    @DeleteMapping("/my-manuscripts/delete")
    public ResponseEntity<?> deleteManuscript(@RequestBody SpecificManuscriptDto request)
            throws NoAvailableDataForGivenInputException, InvalidDataException {

        //replace this with method for extracting user id from jwt;
        UUID userId = UUID.fromString("77872158-dafb-4b4d-aa56-0ea99c5bf91f");

        manuscriptService.deleteManuscript(request, userId);

        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    @GetMapping("/my-manuscripts/download-original")
    public ResponseEntity<?> downloadSpecificManuscript(@RequestBody SpecificManuscriptDto request)
            throws NoAvailableDataForGivenInputException, InvalidDataException, IOException {

        //replace this with method for extracting user id from jwt;
        UUID userId = UUID.fromString("77872158-dafb-4b4d-aa56-0ea99c5bf91f");

        FileSystemResource manuscript =  manuscriptService.downloadOriginalManuscript(request, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .contentLength(manuscript.contentLength())
                .body(manuscript);
    }
}
