package ro.uaic.info.romandec.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "man_metadata")
public class ManuscriptMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String author;
    private String title;
    private String description;
    private Integer yearOfPublication;

}
