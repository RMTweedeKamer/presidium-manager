package nl.th8.presidium.user;

public class InvalidSecretException extends Throwable {
    public InvalidSecretException(String the_entered_secret_is_incorrect) {
    }
}
