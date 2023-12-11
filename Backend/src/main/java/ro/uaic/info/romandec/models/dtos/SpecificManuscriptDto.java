package ro.uaic.info.romandec.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class SpecificManuscriptDto {

    private UUID manuscriptId;

    private String name;

}
