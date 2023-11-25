package ro.uaic.info.romandec.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;
import java.util.UUID;


@Entity
@Getter
@Setter
@Builder
@ToString
@Table(name = "manuscripts")
@NoArgsConstructor
@AllArgsConstructor
public class Manuscript {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String pathToImage;
    private String pathToDecipheredText;

    @OneToOne
    private ManuscriptMetadata manuscriptMetadata;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Manuscript that = (Manuscript) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
