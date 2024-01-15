package ro.uaic.info.romandec.models.dtos;

import lombok.Data;

@Data
public class LoginDto {
    private String email;
    private String password;
}
