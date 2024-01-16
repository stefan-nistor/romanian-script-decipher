package ro.uaic.info.romandec.models.dtos;

import lombok.*;

import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ThirdPartyManuscriptDto {
    private UUID uuid;
    private String message;
    private String filename;
    private String errors;
    private Long jobId;
    private Long docId;
    private Long textRecognitionJobId;
}
