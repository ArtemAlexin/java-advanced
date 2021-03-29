package info.kgeorgiy.ja.alyokhin.jarimplementor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.function.Function;

import static info.kgeorgiy.ja.alyokhin.jarimplementor.Utils.SUPER_STATEMENT;

/**
 * Class which extends {@link AbstractExecutableGenerator}.
 * It is used to generate {@link String} representation of the {@link Constructor}.
 */
public class ConstructorGenerator extends AbstractExecutableGenerator<Constructor<?>> {
    /**
     * Instance of {@link Function}.
     * It is used to get constructor name by the type token.
     */
    private final Function<Class<?>, String> classNameGenerator;

    /**
     * Constructor which initialize <code>classNameGenerator</code>.
     *
     * @param classNameGenerator {@link Function} that creates name of constructor by type token.
     */
    public ConstructorGenerator(Function<Class<?>, String> classNameGenerator) {
        this.classNameGenerator = classNameGenerator;
    }

    /**
     * Returns {@link String} representation of <var>parameter</var> without type.
     *
     * @param parameter {@link Parameter} to be represented.
     * @return parameter representation.
     */
    private String generateParameterWithoutType(Parameter parameter) {
        return parameter.getName();
    }

    /**
     * Returns empty {@link String}, because constructor do not return any value.
     *
     * @param constructor {@link Constructor} return value of which must be generated.
     * @return empty return value.
     */
    @Override
    protected String generateReturn(Constructor<?> constructor) {
        return "";
    }

    /**
     * Returns {@link String} name of <var>constructor</var> using <code>generator</code>
     * which wha passed to this object during initialisation.
     *
     * @param constructor {@link Constructor} name of which must be generated.
     * @return name of constructor.
     */
    @Override
    protected String generateName(Constructor<?> constructor) {
        return classNameGenerator.apply(constructor.getDeclaringClass());
    }

    /**
     * Returns {@link String} representation of <var>constructor</var> main part(between brackets).
     *
     * @param constructor {@link Constructor} main part of which must be generated.
     * @return constructor representation.
     */
    @Override
    protected String generateMain(Constructor<?> constructor) {
        return SUPER_STATEMENT + generateParameters(constructor, this::generateParameterWithoutType);
    }
}
