package info.kgeorgiy.ja.alyokhin.walk;

import java.io.IOException;
import java.util.Arrays;

public class ProcessingFileException extends IOException {
    public ProcessingFileException(final String message) {
        super(message);
    }

    public ProcessingFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
