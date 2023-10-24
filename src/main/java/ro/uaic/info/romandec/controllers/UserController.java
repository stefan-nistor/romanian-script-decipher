package ro.uaic.info.romandec.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.uaic.info.romandec.models.User;
import ro.uaic.info.romandec.services.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        return ResponseEntity.accepted().build();
    }

    public ResponseEntity<?> login(@RequestBody String email, String password) {
        return ResponseEntity.accepted().build();
    }

    @PutMapping
    public ResponseEntity<?> editUser(@RequestBody User user) {
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser(@RequestBody User user) {
        return ResponseEntity.accepted().build();
    }

}
