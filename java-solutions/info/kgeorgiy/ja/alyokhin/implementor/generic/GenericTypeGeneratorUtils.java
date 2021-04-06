package info.kgeorgiy.ja.alyokhin.implementor.generic;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class with static methods for generation generic types for classes and methods.
 */
public class GenericTypeGeneratorUtils {
    /**
     * Private constructor for Utils class.
     */
    private GenericTypeGeneratorUtils() {
    }

    /**
     * Instance of {@link Collector} used for joining {@link java.util.stream.Stream} of strings.
     * Delimiter is {@code ", "}. Prefix is {@code "<"}. Suffix is {@code ">"}.
     * If no strings is necessary to collect collector returns empty string.
     */
    private static final Collector<CharSequence, StringJoiner, String> COLLECTOR = Collector.of(
            () -> new StringJoiner(", ", "<", ">").setEmptyValue(""),
            StringJoiner::add,
            StringJoiner::merge,
            StringJoiner::toString
    );


    /**
     * Returns name of type represented by <var>type</var>.
     * For example. If type represents {@code Arraylist<? extends Integer>}
     * then {@code "ArrayList<? extends Integer>"} is returned.
     *
     * @param type {@link Type} object which name must be returned.
     * @return {@link String} representing name of <var>type</var>.
     * @throws InvalidClassNameException if any errors occurred during resolving name of <var>type</var>.
     */
    public static String getClassName(Type type) {
        if (type instanceof Class<?>) {
            return ((Class<?>) type).getCanonicalName();
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            try {
                return Class.forName(parameterizedType.getRawType().getTypeName()).getCanonicalName() +
                        getNameStream(parameterizedType.getActualTypeArguments()).collect(COLLECTOR);
            } catch (ClassNotFoundException e) {
                throw new InvalidClassNameException("Unable to resolve class " + type.getTypeName(), e);
            }
        }
        if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            Type[] bounds = wildcardType.getLowerBounds();
            if (bounds.length == 0) {
                return getBounds(wildcardType.getUpperBounds(), true);
            } else {
                return getBounds(bounds, false);
            }
        }
        return type.getTypeName();
    }

    /**
     * Returns wildcard {@link String} representation of <var>types</var> joined by {@code ", "}
     *
     * @param types   array which representation must be generated.
     * @param isUpper flag what bounds must be returned {@code "? extends"} or {@code "? super"}
     * @return string representation of array wildcard.
     */
    private static String getBounds(Type[] types, boolean isUpper) {
        return (isUpper ? "? extends " : "? super ") +
                Arrays.stream(types).map(GenericTypeGeneratorUtils::getClassName).collect(Collectors.joining(", "));
    }

    /**
     * Return {@link Stream} of Strings, each string is
     * element of <var>types</var> mapped by {@link GenericTypeGeneratorUtils#getClassName}.
     *
     * @param types array which will be converted to stream.
     * @return stream os string.
     */
    private static Stream<String> getNameStream(Type[] types) {
        return Arrays.stream(types).map(GenericTypeGeneratorUtils::getClassName);
    }

    /**
     * Returns generic type declaration for <var>typeVariables</var>.
     * Example. Let <var>typeVariables</var> is array of {@code T, E}.
     * and {@code T extends Comparable<? super T>}, {@code E extends ArrayList<T>}.
     * Then method will return {@code "<T extends Comparable<? super T>, E extends ArrayList<T>>"}.
     *
     * @param typeVariables an array representing {@link TypeVariable}.
     * @return {@link String} that represents generic type declaration.
     */
    private static String getName(TypeVariable<?>[] typeVariables) {
        return Arrays.stream(typeVariables).map(x ->
                x.getName() + " extends " +
                        Arrays.stream(x.getBounds()).
                                map(GenericTypeGeneratorUtils::getClassName).
                                collect(Collectors.joining("&"))).
                collect(COLLECTOR);
    }

    /**
     * Returns generic type declaration the same as in class represented by <var>token</var>.
     * For class parameters {@link GenericTypeGeneratorUtils#getName} is invoked.
     *
     * @param token type token of the class.
     * @return {@link String} representing generic types declaration.
     */
    public static String generateClassTypeParameters(Class<?> token) {
        return getName(token.getTypeParameters());
    }

    /**
     * Returns generic type names the same as in class represented by <var>token</var>.
     * Example. If class <var>token</var> had parameters
     * {@code <T, E, R>} then {@code "<T, E, R>"} will be returned.
     *
     * @param token type token representing class
     * @return {@link String} string that represents type names.
     */
    public static String generateClassExtensionParameters(Class<?> token) {
        return Arrays.stream(token.getTypeParameters())
                .map(TypeVariable::getName).collect(COLLECTOR);
    }

    /**
     * Returns generic type declaration the same as in <var>executable</var>.
     * For generating parameters types {@link GenericTypeGeneratorUtils#getName} is invoked.
     *
     * @param executable {@link Executable} type declaration of which must be generated.
     * @return {@link String} representation of types.
     */
    public static String generateExecutableTypeParameters(Executable executable) {
        return getName(executable.getTypeParameters());
    }

    /**
     * Returns String representing type of <var>parameter</var>.
     * For getting representation {@link GenericTypeGeneratorUtils#getClassName} is invoked.
     *
     * @param parameter {@link Parameter} which type representation must be returned.
     * @return {@link String} representation of <var>parameter</var>
     */
    public static String generateType(Parameter parameter) {
        return getClassName(parameter.getParameterizedType());
    }
}
