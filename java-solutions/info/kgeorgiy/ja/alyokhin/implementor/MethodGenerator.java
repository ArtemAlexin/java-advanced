package info.kgeorgiy.ja.alyokhin.implementor;

import java.lang.reflect.Method;

import static info.kgeorgiy.ja.alyokhin.implementor.Utils.*;

public class MethodGenerator extends AbstractExecutableGenerator<Method> {
    private static final Formatter formatter = new Formatter();

    private static String getReturnedValue(Class<?> token) {
        return SPECIAL_PRIMITIVE_RETURN_VALUES.
                getOrDefault(token, token.isPrimitive() ? "0" : "null");
    }

    @Override
    protected String generateReturn(Method method) {
        return method.getReturnType().getCanonicalName();
    }

    @Override
    protected String generateName(Method method) {
        return method.getName();
    }

    @Override
    protected String generateMain(Method method) {
        return formatter.setWord(RETURN_STATEMENT).
                setTextPart(getReturnedValue(method.getReturnType())).
                getFormattedText();
    }
}
