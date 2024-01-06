package ro.uaic.info.romandec.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ro.uaic.info.romandec.models.Manuscript;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public interface ManuscriptRepository extends JpaRepository<Manuscript,Integer> {

    @Query(value = "SELECT path_to_image FROM manuscripts WHERE " +
            "manuscripts.path_to_deciphered_text is null " +
            "ORDER BY random() LIMIT 1", nativeQuery = true)
    String getRandomNotDecipheredManuscript();

    Optional<Manuscript> getManuscriptByPathToImage(String pathToOriginalImage);

    Optional<Manuscript> getManuscriptByIdAndNameAndUserId(UUID id, String name, UUID userId);

    List<Manuscript> getAllByUserId(UUID userId);
}
