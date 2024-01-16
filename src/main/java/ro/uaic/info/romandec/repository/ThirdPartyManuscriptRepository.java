package ro.uaic.info.romandec.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.uaic.info.romandec.models.ThirdPartyManuscript;

import java.util.Optional;
import java.util.UUID;

public interface ThirdPartyManuscriptRepository extends JpaRepository<ThirdPartyManuscript, UUID> {
    Optional<ThirdPartyManuscript> findThirdPartyManuscriptByDocId(Long docId);
    Optional<ThirdPartyManuscript> findThirdPartyManuscriptByTextRecognitionJobId(Long id);
}
