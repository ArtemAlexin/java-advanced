package info.kgeorgiy.ja.alyokhin.walk;

import java.io.IOException;

public class ProcessingFileException extends IOException {
    public ProcessingFileException(final String message) {
        super(message);
    }

    public ProcessingFileException(final String message, final Throwable cause) {
        super(message + ": " + cause.getMessage(), cause);
    }
}
