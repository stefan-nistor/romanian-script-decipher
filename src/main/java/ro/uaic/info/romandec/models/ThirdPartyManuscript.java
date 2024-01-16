package ro.uaic.info.romandec.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class ThirdPartyManuscript {
    @Id
    @GeneratedValue
    private UUID uuid;
    private String message;
    private String filename;
    private String errors;
    private Long jobId;
    private Long docId;
    private Long textRecognitionJobId;
}
