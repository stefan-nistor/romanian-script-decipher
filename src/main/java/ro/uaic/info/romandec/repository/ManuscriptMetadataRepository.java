package ro.uaic.info.romandec.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.uaic.info.romandec.models.ManuscriptMetadata;

public interface ManuscriptMetadataRepository extends JpaRepository<ManuscriptMetadata, Integer> {
}
