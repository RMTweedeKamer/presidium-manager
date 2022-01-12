package nl.th8.presidium.user.service;

import nl.th8.presidium.user.InvalidSecretException;
import nl.th8.presidium.user.UsernameExistsException;
import nl.th8.presidium.user.controller.dto.User;
import nl.th8.presidium.user.data.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository repository;

    private final PasswordEncoder passwordEncoder;

    private final String secret;
    private final String rvsSecret;

    @Autowired
    public UserService(UserRepository repository, @Lazy PasswordEncoder passwordEncoder, @Value("${manager.user-secret}") String secret, @Value("${manager.rvs-secret}") String rvsSecret) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.secret = secret;
        this.rvsSecret = rvsSecret;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = repository.findById(username);

        if(user.isPresent()) {
            return new org.springframework.security.core.userdetails.User(user.get().getUsername(), user.get().getPassword(), user.get().getAuthorityList());
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }

    public void createUser(User newUser) throws UsernameExistsException, InvalidSecretException {
        if(usernameExists(newUser.getUsername())) {
            throw new UsernameExistsException();
        }
        if(newUser.getSecret().equals(this.secret)) {
            newUser.getAuthorityList().add(new SimpleGrantedAuthority("ADMIN"));
        }
        else if(newUser.getSecret().equals(this.rvsSecret)) {
            newUser.getAuthorityList().add(new SimpleGrantedAuthority("RVS"));
        }
        else {
            throw new InvalidSecretException();
        }
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        repository.insert(newUser);
    }

    private boolean usernameExists(String username) {
        Optional<User> user = repository.findById(username);
        return user.isPresent();
    }
}
