package ro.uaic.info.romandec.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.uaic.info.romandec.services.ManuscriptService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    public ManuscriptService manuscriptService;

    @Autowired
    public AdminController(ManuscriptService manuscriptService)
    {
        this.manuscriptService = manuscriptService;
    }

}
