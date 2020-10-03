package nl.th8.presidium;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Optional;

@Service
public class ControllerUtils {

    private final RedditSupplier redditSupplier;

    @Autowired
    public ControllerUtils(RedditSupplier redditSupplier) {
        this.redditSupplier = redditSupplier;
    }

    public boolean checkLoggedIn(Principal principal) {
        try {
            return principal.getName().length() > 0;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public Optional<String> checkRedditStatus() {
        if(redditSupplier.redditDown)
            return Optional.of("error-reddit");
        else
            return Optional.empty();
    }
}
