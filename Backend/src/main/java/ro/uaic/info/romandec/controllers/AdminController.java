package ro.uaic.info.romandec.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ro.uaic.info.romandec.services.ManuscriptService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    public ManuscriptService manuscriptService;

    @Autowired
    public AdminController(ManuscriptService manuscriptService)
    {
        this.manuscriptService = manuscriptService;
    }

    @PostMapping("/initializeTest")
    public ResponseEntity<?> initializeTestData(@RequestParam("images") List<MultipartFile> images)
    {
        ResponseEntity<?> response;
        try
        {
            boolean success = manuscriptService.initializeTestData(images);
            response = success ?
                    ResponseEntity.status(HttpStatus.OK).body("Success at initialization.") :
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed at initialization.");
        }
        catch (Exception e)
        {
            response = ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
        return response;
    }
}
