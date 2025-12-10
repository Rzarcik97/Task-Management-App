package taskmanagement.exceptions;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class RegistrationException extends RuntimeException {
    public RegistrationException(String message) {
        super(message);
        log.warn(message);
    }
}
