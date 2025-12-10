package taskmanagement.exceptions;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class EmailSendingException extends RuntimeException {
    public EmailSendingException(String message) {
        super(message);
        log.error(message);
    }
}
