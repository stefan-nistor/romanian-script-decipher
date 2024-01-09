package ro.uaic.info.romandec.models.dtos;

import lombok.*;

import java.util.UUID;

@Data
@Builder
public class ManuscriptPreviewResponseDto {

    private UUID manuscriptId;

    private String title;

    private String author;

    private Integer yearOfPublication;

}
