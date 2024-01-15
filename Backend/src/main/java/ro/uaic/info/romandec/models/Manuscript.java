package ro.uaic.info.romandec.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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
@Table(name = "manuscripts")
public class Manuscript {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String pathToOriginalText;
    private String pathToDecipheredText;

    @Column(unique=true)
    private String filename;

    @OneToOne
    private ManuscriptMetadata manuscriptMetadata;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

}
