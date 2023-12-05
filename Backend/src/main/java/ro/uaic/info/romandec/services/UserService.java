package ro.uaic.info.romandec.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.uaic.info.romandec.repository.UserRepository;
import ro.uaic.info.romandec.models.User;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public User createUser(User userDto) {
        return new User();
    }

    public void deleteUser(User userDto) {
        return;
    }

    public User editUser(User userDto) {
        return new User();
    }

    public UUID getTestUserUUID() {
        return userRepository.getFirstByIdNotNull().getId();
    }
}
