package ro.uaic.info.romandec.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.uaic.info.romandec.models.User;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createUser(User userDto) {
        return new User();
    }

    public void deleteUser(User userDto) {
        return;
    }

    public User editUser(User userDto) {
        return new User();
    }

}
