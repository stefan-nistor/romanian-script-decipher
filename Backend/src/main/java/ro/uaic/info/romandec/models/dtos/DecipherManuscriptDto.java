package ro.uaic.info.romandec.models.dtos;

import lombok.Data;

@Data
public class DecipherManuscriptDto {

    private String title;

    private String author;

    private Integer yearOfPublication;

    private String description;
}
