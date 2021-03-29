package info.kgeorgiy.ja.alyokhin.jarimplementor;

import java.util.Map;

/**
 * Class with usedful constants
 */
public class Utils {
    /**
     * private constructor without parameters to forbid inheritance and installation.
     */
    private Utils() {
    }

    /**
     * Constant equal to {@code "super"} for code generation.
     */
    public static final String SUPER_STATEMENT = "super";

    /**
     * Constant equal to one semicolon for code generation.
     */
    public static final String SEMICOLON = ";";

    /**
     * Constant equal to one space for code generation.
     */
    public static final String SPACE = " ";

    /**
     * Constant equal to {@code "extends"} for code generation.
     */
    public static final String EXTENDS_STATEMENT = "extends";

    /**
     * Constant equal to {@code "implements"} for code generation.
     */
    public static final String IMPLEMENTS_STATEMENT = "implements";

    /**
     * Constant equal to {@code "return"} for code generation.
     */
    public static final String RETURN_STATEMENT = "return";

    /**
     * Constant equal to {@code "throws"} for code generation.
     */
    public static final String THROWS_STATEMENT = "throws";

    /**
     * Constant equal to {@code "package"} for code generation.
     */
    public static final String PACKAGE_PREFIX = "package";

    /**
     * Constant equal to one tabulation for code generation.
     */
    public static final String TAB = "\t";

    /**
     * Constant equal to {@link System#lineSeparator()} for code generation.
     */
    public static final String EOL = System.lineSeparator();

    /**
     * Constant equal to one right bracket for code generation.
     */
    public static final String RBRACKET = "}";

    /**
     * Constant equal to one left bracket for code generation.
     */
    public static final String LBRACKET = "{";

    /**
     * Constant equal to {@code "public class"} for code generation.
     */
    public static final String PUBLIC_NAME = "public class";

    /**
     * Map with {@link String} representation of some primitives special return values.
     */
    public static final Map<Class<?>, String> SPECIAL_PRIMITIVE_RETURN_VALUES = Map.of(
            boolean.class, "false",
            void.class, ""
    );

    /**
     * Constant equal to {@code "implementor"} for code generation.
     */
    public static final String FOLDER_TO_COMPILE = "implementor";

    /**
     * Constant equal to {@code "tmp"} for code generation.
     */
    public static final String TMP_FOLDER = "tmp";

    /**
     * Constant equal to one forward slash for code generation.
     */
    public static final String JarFileSeparator = "/";
}
