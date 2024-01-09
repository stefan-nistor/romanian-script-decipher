package ro.uaic.info.romandec.models.dtos;

import lombok.*;

import java.util.UUID;

@Data
@Builder
public class ManuscriptDetailedResponseDto {

    private UUID manuscriptId;

    private String name;

    private String title;

    private String author;

    private String description;

    private Integer yearOfPublication;

}
