package info.kgeorgiy.ja.alyokhin.implementor;

import info.kgeorgiy.ja.alyokhin.implementor.generic.GenericTypeGeneratorUtils;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import static info.kgeorgiy.ja.alyokhin.implementor.Utils.THROWS_STATEMENT;
/**
 * This class provides a skeletal implementation of generating {@link String} representation
 * of {@link Executable} object.
 * To implement this class you should only extend
 * {@link #generateReturn}, {@link #generateName}, {@link #generateMain}.
 *
 * @param <T> the type of {@code Executable} to generate implementation.
 */
public abstract class AbstractExecutableGenerator<T extends Executable> {
    /**
     * Returns string representing the <var>parameter</var>(type and name).
     * Uses {@link GenericTypeGeneratorUtils#generateType}.
     *
     * @param parameter {@link Parameter} of the executable.
     * @return string representation.
     */
    private String generateParameterWithType(Parameter parameter) {
        return new Formatter()
                .setWord(GenericTypeGeneratorUtils.generateType(parameter))
                .setTextPart(parameter.getName())
                .getFormattedText();
    }

    /**
     * Method to be implemented.
     * Generates return type of the <var>executable</var>.
     *
     * @param executable parameter returned value of which should be generated.
     * @return {@link String} representation of the returned type.
     */
    protected abstract String generateReturn(T executable);

    /**
     * Method to be implemented.
     * Generates name of the <var>executable</var>.
     *
     * @param executable parameter name of which should be generated.
     * @return {@link String} representation of <var>executable</var> name.
     */
    protected abstract String generateName(T executable);

    /**
     * Method to be implemented.
     * Generates main part of the <var>executable</var>(part inside brackets).
     *
     * @param executable parameter main part of which should be generated.
     * @return {@link String} representation of the.
     */
    protected abstract String generateMain(T executable);

    /**
     * Returns {@link String} representation of <var>executable</var> parameters mapped by <var>genFunction</var>.
     *
     * @param executable  object {@link Parameter} of which should be generated.
     * @param genFunction function mapping {@code Parameter} to {@code String}.
     * @return parameters representation.
     */
    protected String generateParameters(T executable, Function<Parameter, String> genFunction) {
        return Arrays.stream(executable.getParameters()).
                map(genFunction).
                collect(Collectors.joining(", ", "(", ")"));
    }

    /**
     * Returns {@link String} representation of exceptions which <var>executable</var> throws.
     * Example <code>throws IOException</code>.
     *
     * @param executable object exceptions of which should be generated.
     * @return exception representation.
     */
    protected String generateException(T executable) {
        Formatter formatter = new Formatter();
        Class<?>[] exceptions = executable.getExceptionTypes();
        if (exceptions.length > 0) {
            formatter.setWord(THROWS_STATEMENT);
        }
        formatter.setTextPart(Arrays.stream(exceptions).
                map(Class::getCanonicalName).collect(Collectors.joining(", ")));
        return formatter.getFormattedText();
    }

    /**
     * Return {@link String} representation of <var>executable</var>.
     * Uses {@link #generateMain}, {@link #generateName}, {@link #generateReturn}, {@link #generateException},
     * {@link #generateParameters}, {@link #generateParameterWithType} to create representation.
     * Also invokes {@link GenericTypeGeneratorUtils#generateExecutableTypeParameters}
     *
     * @param executable object implementation of which should be generated.
     * @return representaion of <var>executable</var>
     */
    public String createExecutable(T executable) {
        int mod = executable.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.NATIVE & ~Modifier.TRANSIENT;
        return new Formatter().setTabulation()
                .setWordIfPresent(Modifier.toString(mod))
                .setWord(GenericTypeGeneratorUtils.generateExecutableTypeParameters(executable))
                .setWordIfPresent(generateReturn(executable))
                .setTextPart(generateName(executable))
                .setWord(generateParameters(executable, this::generateParameterWithType))
                .setBlockBeginning(generateException(executable))
                .setTabulation(2)
                .setBlockEnding(generateMain(executable)).getFormattedText();
    }
}
