package info.kgeorgiy.ja.alyokhin.implementor;

import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import static info.kgeorgiy.ja.alyokhin.implementor.Utils.THROWS_STATEMENT;

public abstract class AbstractExecutableGenerator<T extends Executable> {
    private String generateParameterWithType(Parameter parameter) {
        return new Formatter().setWord(parameter.getType()
                .getCanonicalName()).setTextPart(parameter.getName()).getFormattedText();
    }

    protected abstract String generateReturn(T executable);

    protected abstract String generateName(T executable);

    protected abstract String generateMain(T executable);

    protected String generateParameters(T executable, Function<Parameter, String> genFunction) {
        return Arrays.stream(executable.getParameters()).
                map(genFunction).
                collect(Collectors.joining(", ", "(", ")"));
    }

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

    public String createExecutable(T executable) {
        int mod = executable.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.NATIVE & ~Modifier.TRANSIENT;
        return new Formatter().setTabulation()
                .setWordIfPresent(Modifier.toString(mod))
                .setWordIfPresent(generateReturn(executable))
                .setTextPart(generateName(executable))
                .setWord(generateParameters(executable, this::generateParameterWithType))
                .setBlockBeginning(generateException(executable))
                .setTabulation(2)
                .setBlockEnding(generateMain(executable)).getFormattedText();
    }
}
