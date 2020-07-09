package nl.th8.presidium.user.service;

import nl.th8.presidium.Constants;
import nl.th8.presidium.user.InvalidSecretException;
import nl.th8.presidium.user.UsernameExistsException;
import nl.th8.presidium.user.controller.dto.User;
import nl.th8.presidium.user.data.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    UserRepository repository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private String secret;

    @Autowired
    public UserService(
            @Value("${manager.user-secret}") String secret
    ) {
        this.secret = secret;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = repository.findById(username);

        if(user.isPresent()) {
            List<GrantedAuthority> authorityList = Arrays.asList();

            return new org.springframework.security.core.userdetails.User(user.get().getUsername(), user.get().getPassword(), authorityList);
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }

    public void createUser(User newUser) throws UsernameExistsException, InvalidSecretException {
        if(usernameExists(newUser.getUsername())) {
            throw new UsernameExistsException("There is an account with that username: " +  newUser.getUsername());
        }
        if(!newUser.getSecret().equals(this.secret)) {
            throw new InvalidSecretException("The entered secret is incorrect");
        }
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        repository.insert(newUser);
    }

    private boolean usernameExists(String username) {
        Optional user = repository.findById(username);
        return user.isPresent();
    }

    public User getUser(String username) {
        Optional<User> user = repository.findById(username);

        if(user.isPresent()) {
            return user.get();
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }

    public void verifiyUser(String username) {
        Optional<User> user = repository.findById(username);

        if(user.isPresent()) {
            User modifiedUser = user.get();
            modifiedUser.grantAuthority("USER");
            modifiedUser.setVerified(true);
            repository.save(modifiedUser);
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }
}
