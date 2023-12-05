package ro.uaic.info.romandec.Response;

import lombok.*;

import java.util.UUID;

@Builder
@AllArgsConstructor
@Getter
@Setter
@RequiredArgsConstructor
public class ManuscriptPreviewResponse {

    private UUID manuscriptId;

    private String name;
}
