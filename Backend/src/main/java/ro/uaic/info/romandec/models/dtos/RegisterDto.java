package ro.uaic.info.romandec.models.dtos;

import lombok.Data;

@Data
public class RegisterDto {
    private String email;
    private String password;
    private String firstname;
    private String lastname;
}
