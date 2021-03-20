package info.kgeorgiy.ja.alyokhin.implementor;

import java.util.Map;

public class Utils {
    private Utils() {
    }

    public static final String SUPER_STATEMENT = "super";
    public static final String SEMICOLON = ";";
    public static final String SPACE = " ";
    public static final String EXTENDS_STATEMENT = "extends";
    public static final String IMPLEMENTS_STATEMENT = "implements";
    public static final String RETURN_STATEMENT = "return";
    public static final String THROWS_STATEMENT = "throws";
    public static final String PACKAGE_PREFIX = "package";
    public static final String TAB = "\t";
    public static final String EOL = System.lineSeparator();
    public static final String RBRACKET = "}";
    public static final String LBRACKET = "{";
    public static final String PUBLIC_NAME = "public class";
    public static final Map<Class<?>, String> SPECIAL_PRIMITIVE_RETURN_VALUES = Map.of(
            boolean.class, "false",
            void.class, ""
    );
}
