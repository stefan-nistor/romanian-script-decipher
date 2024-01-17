package ro.uaic.info.romandec.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    private static final UUID USER_ID = UUID.fromString("dff34814-4b40-410c-b8c3-c7652f503fbd");

    @Autowired
    public ProfileController(ManuscriptService manuscriptService) {
        this.manuscriptService = manuscriptService;
    }

    @GetMapping("/my-manuscripts/all")
    public ResponseEntity<Object> getAllUsersManuscripts() {
        List<ManuscriptPreviewResponseDto> allUsersManuscripts = manuscriptService.getAllUsersManuscripts(USER_ID);

        if (allUsersManuscripts.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("This user has no manuscripts added.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(allUsersManuscripts);
    }

    @GetMapping("/my-manuscripts/specific/{manuscriptId}")
    public ResponseEntity<Object> getSpecificManuscript(@PathVariable UUID manuscriptId) {
        ManuscriptDetailedResponseDto manuscript = manuscriptService.getSpecificManuscript(manuscriptId, USER_ID);

        return ResponseEntity.status(HttpStatus.OK).body(manuscript);
    }

    @DeleteMapping("/my-manuscripts/delete/{manuscriptId}")
    public ResponseEntity<Object> deleteManuscript(@PathVariable UUID manuscriptId) {
        manuscriptService.deleteManuscript(manuscriptId, USER_ID);

        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    @GetMapping("/my-manuscripts/download-original/{manuscriptId}")
    public ResponseEntity<Object> downloadSpecificManuscript(@PathVariable UUID manuscriptId) throws IOException {
        FileSystemResource manuscript = manuscriptService.downloadOriginalManuscript(manuscriptId, USER_ID);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .contentLength(manuscript.contentLength())
                .body(manuscript);
    }
}
