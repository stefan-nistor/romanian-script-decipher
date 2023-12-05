package ro.uaic.info.romandec.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class SpecificManuscriptRequest {

    private UUID manuscriptId;

    private String name;

}
