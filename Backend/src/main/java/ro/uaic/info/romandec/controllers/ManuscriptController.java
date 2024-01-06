package ro.uaic.info.romandec.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.uaic.info.romandec.models.Manuscript;
import ro.uaic.info.romandec.services.ManuscriptService;

import java.util.UUID;

@RestController
@RequestMapping("/api/manuscript")
public class ManuscriptController {

}
