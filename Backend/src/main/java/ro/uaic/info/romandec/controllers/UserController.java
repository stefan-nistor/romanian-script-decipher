package ro.uaic.info.romandec.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.uaic.info.romandec.models.User;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @PutMapping
    public ResponseEntity<?> editUser(@RequestBody User user) {
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser(@RequestBody User user) {
        return ResponseEntity.accepted().build();
    }

}
