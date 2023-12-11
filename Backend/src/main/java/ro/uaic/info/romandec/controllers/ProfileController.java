package ro.uaic.info.romandec.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
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
    public ResponseEntity<?> getAllUsersManuscripts(@RequestParam("userId") UUID userId) {

        try {

            List<ManuscriptPreviewResponseDto> allUsersManuscripts = manuscriptService.getAllUsersManuscripts(userId);

            if (allUsersManuscripts.isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("This user has no manuscripts added.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(allUsersManuscripts);
        } catch (NoAvailableDataForGivenInputException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/my-manuscripts")
    public ResponseEntity<?> getSpecificManuscript(@RequestBody SpecificManuscriptDto request, @RequestParam UUID userId) {

        try {
            ManuscriptDetailedResponseDto manuscript = manuscriptService.getSpecificManuscript(request, userId);

            return ResponseEntity.status(HttpStatus.OK).body(manuscript);
        } catch (NoAvailableDataForGivenInputException | InvalidDataException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/my-manuscript/delete")
    public ResponseEntity<?> deleteSpecificManuscript(@RequestBody SpecificManuscriptDto request, @RequestParam UUID userId) {

        try {
            manuscriptService.deleteSpecificManuscript(request, userId);

            return ResponseEntity.status(HttpStatus.OK).body("");
        } catch (NoAvailableDataForGivenInputException | InvalidDataException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/my-manuscripts/download")
    public ResponseEntity<?> downloadSpecificManuscript(@RequestBody SpecificManuscriptDto request, @RequestParam UUID userId){

        try {
            FileSystemResource manuscript =  manuscriptService.downloadSpecificManuscript(request, userId);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(manuscript);

        } catch (NoAvailableDataForGivenInputException | InvalidDataException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
