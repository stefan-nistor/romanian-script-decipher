package ro.uaic.info.romandec.Response;

import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class ManuscriptDetailedResponse {

    private UUID manuscriptId;

    private String name;

    boolean deciphered;

    private String title;

    private String author;

    private String description;
}
