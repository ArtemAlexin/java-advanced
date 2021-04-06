package info.kgeorgiy.ja.alyokhin.implementor.generic;

/**
 * Exception which is thrown by {@link GenericTypeGeneratorUtils#getClassName}
 */
public class InvalidClassNameException extends RuntimeException {
    /**
     * Constructs {@link InvalidClassNameException} from given <var>message</var> and <var>cause</var>.
     *
     * @param message message to be wrapped.
     * @param cause   cause to be set.
     */
    public InvalidClassNameException(String message, Throwable cause) {
        super(message, cause);
    }
}
