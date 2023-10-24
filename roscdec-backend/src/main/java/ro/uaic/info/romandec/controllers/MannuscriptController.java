package ro.uaic.info.romandec.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.uaic.info.romandec.models.Manuscript;
import ro.uaic.info.romandec.services.MannuscriptService;

import java.util.UUID;

@RestController
public class MannuscriptController {

    @Autowired
    private MannuscriptService mannuscriptService;

    public ResponseEntity<?> uploadMannuscript(@RequestBody Manuscript manuscript) {
        return ResponseEntity.accepted().build();
    }

    public ResponseEntity<?> getDeciphered(@RequestParam UUID manuscriptId) {
        return ResponseEntity.accepted().build();
    }

    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok().build();
    }

}
