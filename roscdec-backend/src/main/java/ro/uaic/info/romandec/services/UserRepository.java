package ro.uaic.info.romandec.services;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.uaic.info.romandec.models.User;


import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
}
