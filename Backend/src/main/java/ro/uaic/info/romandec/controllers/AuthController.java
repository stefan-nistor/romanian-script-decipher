package ro.uaic.info.romandec.controllers;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ro.uaic.info.romandec.models.dtos.LoginDto;
import ro.uaic.info.romandec.models.dtos.RegisterDto;
import ro.uaic.info.romandec.services.impl.UserDetailServiceImpl;

import java.util.Date;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserDetailServiceImpl userDetailService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(UserDetailServiceImpl userDetailService, AuthenticationManager authenticationManager) {
        this.userDetailService = userDetailService;
        this.authenticationManager = authenticationManager;
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDto registerDto) {
        var userId = userDetailService.registerUser(registerDto);
        var uri = ServletUriComponentsBuilder.fromUriString("/api/users").path("/{userId}").buildAndExpand(userId).toUri();
        return ResponseEntity.created(uri).body("User registered successfully. User ID:" + userId);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getEmail(), loginDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        var userDetails = (UserDetails) authentication.getPrincipal();
        return new ResponseEntity<>(generateJwt(userDetails), HttpStatus.OK);
    }

    private String generateJwt(UserDetails userDetails) {
        return "Bearer " + Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 900000))
                .signWith(Jwts.SIG.HS512.key().build()) // currently the signing key is generated at each request. fix needed
                .compact();
    }

}
