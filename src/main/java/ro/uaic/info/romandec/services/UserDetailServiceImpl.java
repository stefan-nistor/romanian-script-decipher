package ro.uaic.info.romandec.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ro.uaic.info.romandec.exceptions.UserAlreadyExistsException;
import ro.uaic.info.romandec.models.User;
import ro.uaic.info.romandec.models.dtos.RegisterDto;
import ro.uaic.info.romandec.repository.RoleRepository;
import ro.uaic.info.romandec.repository.UserRepository;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_DEFAULT = "ROLE_DEFAULT";
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserDetailServiceImpl(UserRepository userRepository, RoleRepository roleRepository, ModelMapper mapper,
                                 PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }


    /**
     * The {@link ro.uaic.info.romandec.models.User} does not have a username field hence using email instead
     * @param email the email identifying the user whose data is required.
     * @return {@link UserDetails} after authentication
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username or email: "+ email));

        Set<GrantedAuthority> authorities = user
                .getRoles()
                .stream()
                .map((role) -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(user.getEmail(),
                user.getPassword(),
                authorities);
    }

    public UUID registerUser(RegisterDto registerDto) {
        if(userRepository.findByEmail(registerDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException();
        }
        var user = mapper.map(registerDto, User.class);
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setRoles(Collections.singleton(roleRepository.findByName(ROLE_ADMIN).orElseThrow()));
        return userRepository.save(user).getId();
    }

}
