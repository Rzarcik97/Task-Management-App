package taskmanagement.exceptions;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TemplatesLoadException extends RuntimeException {
    public TemplatesLoadException(String message) {
        super(message);
        log.error(message);
    }
}
