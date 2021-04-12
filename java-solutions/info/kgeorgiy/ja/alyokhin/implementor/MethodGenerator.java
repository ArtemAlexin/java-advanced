package info.kgeorgiy.ja.alyokhin.implementor;

import info.kgeorgiy.ja.alyokhin.implementor.generic.GenericTypeGeneratorUtils;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static info.kgeorgiy.ja.alyokhin.implementor.Utils.RETURN_STATEMENT;
import static info.kgeorgiy.ja.alyokhin.implementor.Utils.SPECIAL_PRIMITIVE_RETURN_VALUES;

/**
 * Class which extends {@link AbstractExecutableGenerator}.
 * It is used to generate {@link String} representation of the {@link Method}.
 */
public class MethodGenerator extends AbstractExecutableGenerator<Method> {
    /**
     * Instance of {@link Formatter} used for convenient text formatting.
     */
    private static final Formatter formatter = new Formatter();

    /**
     * Returns {@code String} representation of the default value of provided <var>token</var>.
     * Example <code>"null"</code> for any {@link Object}.
     *
     * @param token which represent a class default value of which must be generated.
     * @return default value representation.
     */
    private static String getReturnedValue(Class<?> token) {
        return SPECIAL_PRIMITIVE_RETURN_VALUES.
                getOrDefault(token, token.isPrimitive() ? "0" : "null");
    }

    /**
     * Returns {@link String} representation the provided <var>method</var> return type name.
     * Invokes {@link Method#getGenericReturnType()}.
     *
     * @param method {@code Method} return value of which must be generated.
     * @return type representation.
     */
    @Override
    protected String generateReturn(Method method) {
        return method.getReturnType().getCanonicalName();
    }

    /**
     * Returns {@code String} representation the provided <var>method</var> name.
     * Invokes {@link Method#getName()}.
     *
     * @param method {@link Method} name of which must be generated.
     * @return name representation.
     */
    @Override
    protected String generateName(Method method) {
        return method.getName();
    }

    /**
     * Returns {@code String} representation the provided <var>method</var> main part(between brackets).
     *
     * @param method {@code Method} main part of which must be generated.
     * @return main <var>method</var> part representation.
     */
    @Override
    protected String generateMain(Method method) {
        return formatter.setWord(RETURN_STATEMENT).
                setTextPart(getReturnedValue(method.getReturnType())).
                getFormattedText();
    }
}
