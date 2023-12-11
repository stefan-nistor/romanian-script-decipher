package ro.uaic.info.romandec.models.dtos;

import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class ManuscriptDetailedResponseDto {

    private UUID manuscriptId;

    private String name;

    boolean deciphered;

    private String title;

    private String author;

    private String description;
}
