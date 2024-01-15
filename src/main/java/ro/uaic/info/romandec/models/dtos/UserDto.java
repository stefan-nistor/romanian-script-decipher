package ro.uaic.info.romandec.models.dtos;

import lombok.*;
import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link ro.uaic.info.romandec.models.User}
 */
@Data
public class UserDto implements Serializable {
    private UUID id;
    private String email;

}