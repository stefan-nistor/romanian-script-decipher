package ro.uaic.info.romandec.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ro.uaic.info.romandec.models.Manuscript;

public interface ManuscriptRepository extends JpaRepository<Manuscript,Integer> {

    @Query(value = "SELECT path_to_image FROM manuscripts WHERE " +
            "manuscripts.path_to_annotated_image is null " +
            "ORDER BY random() LIMIT 1", nativeQuery = true)
    String getRandomNotDecipheredManuscript();
}
