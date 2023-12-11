package ro.uaic.info.romandec.models.dtos;

import lombok.*;

import java.util.UUID;

@Builder
@AllArgsConstructor
@Getter
@Setter
@RequiredArgsConstructor
public class ManuscriptPreviewResponseDto {

    private UUID manuscriptId;

    private String name;
}
