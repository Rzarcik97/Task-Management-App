package taskmanagement.exceptions;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class FileStorageException extends RuntimeException {
    public FileStorageException(String message) {
        super(message);
        log.warn(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
        log.warn(message);
    }
}
