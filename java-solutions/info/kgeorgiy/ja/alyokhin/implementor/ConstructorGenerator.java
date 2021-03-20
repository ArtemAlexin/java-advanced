package info.kgeorgiy.ja.alyokhin.implementor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.function.Function;

import static info.kgeorgiy.ja.alyokhin.implementor.Utils.SUPER_STATEMENT;

public class ConstructorGenerator extends AbstractExecutableGenerator<Constructor<?>> {
    private final Function<Class<?>, String> classNameGenerator;

    public ConstructorGenerator(Function<Class<?>, String> classNameGenerator) {
        this.classNameGenerator = classNameGenerator;
    }

    private String generateParameterWithoutType(Parameter parameter) {
        return parameter.getName();
    }

    @Override
    protected String generateReturn(Constructor<?> constructor) {
        return "";
    }

    @Override
    protected String generateName(Constructor<?> constructor) {
        return classNameGenerator.apply(constructor.getDeclaringClass());
    }

    @Override
    protected String generateMain(Constructor<?> constructor) {
        return SUPER_STATEMENT + generateParameters(constructor, this::generateParameterWithoutType);
    }
}
