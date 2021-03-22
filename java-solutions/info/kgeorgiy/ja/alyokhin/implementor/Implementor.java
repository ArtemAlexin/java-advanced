package info.kgeorgiy.ja.alyokhin.implementor;

import static info.kgeorgiy.ja.alyokhin.implementor.Utils.*;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Implementor implements Impler {
    private static final MethodGenerator METHOD_GENERATOR = new MethodGenerator();
    private static final ConstructorGenerator CONSTRUCTOR_GENERATOR = new ConstructorGenerator(Implementor::generateClassName);

    private static String getPackageName(Class<?> token) {
        return Objects.isNull(token) ? "" : token.getPackageName();
    }

    private static String generateClassWithName(Class<?> token, String name) {
        return token.getSimpleName() + name;
    }

    private static String getClassNameImpl(Class<?> token) {
        return generateClassWithName(token, "Impl.java");
    }

    private static String generateClassName(Class<?> token) {
        return generateClassWithName(token, "Impl");
    }

    private static Path getPath(Path path, Class<?> token) {
        return path.resolve(getPackageName(token).
                replace('.', File.separatorChar)).
                resolve(getClassNameImpl(token));
    }

    private static String generatePackage(Class<?> token) {
        Formatter formatter = new Formatter();
        if (!getPackageName(token).isEmpty()) {
            formatter.setWord(PACKAGE_PREFIX).setStatement(getPackageName(token));
        }
        return formatter.setLineEnding().getFormattedText();
    }

    private static String generateExtension(Class<?> token) {
        return token.isInterface() ? IMPLEMENTS_STATEMENT : EXTENDS_STATEMENT;
    }

    private static void checkObjectIsNull(Object obj, String name) throws ImplerException {
        if (Objects.isNull(obj)) {
            throw new ImplerException(name + " is null");
        }
    }


    private static void checkTokenForInheritance(Class<?> token) throws ImplerException {
        if (token.isPrimitive()
                || token.isArray()
                || Modifier.isFinal(token.getModifiers())
                || Modifier.isPrivate(token.getModifiers())
                || token == Enum.class) {
            throw new ImplerException("Unable to implement this class");
        }
    }


    private static void createParent(Path root) throws ImplerException {
        if (root.getParent() != null) {
            try {
                Files.createDirectories(root.getParent());
            } catch (IOException e) {
                throw new ImplerException("Unable to create directories to file", e);
            }
        }
    }

    private static void addMethods(Method[] methods, Set<CustomMethod> set, Predicate<Method> predicate) {
        Arrays.stream(methods).
                filter(predicate.and(Predicate.not(Method::isBridge))).
                map(CustomMethod::new).
                collect(Collectors.toCollection(() -> set));
    }

    private static Set<CustomMethod> getAllMethodWithPredicate(Class<?> token, Predicate<Method> predicate) {
        Set<CustomMethod> methods = new HashSet<>();
        addMethods(token.getMethods(), methods, predicate);
        while (token != null) {
            addMethods(token.getDeclaredMethods(), methods, predicate);
            token = token.getSuperclass();
        }
        return methods;
    }

    private static Constructor<?>[] getConstructors(Class<?> token) {
        return Arrays.stream(token.getDeclaredConstructors()).
                filter(constructor -> !Modifier.isPrivate(constructor.getModifiers())).
                toArray(Constructor[]::new);
    }

    private static void writeAbstractMethods(Class<?> token, Writer writer) throws IOException {
        Set<CustomMethod> methods = getAllMethodWithPredicate(token,
                method -> Modifier.isAbstract(method.getModifiers()));
        methods.removeAll(getAllMethodWithPredicate(token,
                method -> Modifier.isFinal(method.getModifiers())));
        for (CustomMethod method : methods) {
            writer.write(METHOD_GENERATOR.createExecutable(method.getMethod()));
        }
    }

    private static void writeConstructors(Class<?> token, Writer writer) throws IOException, ImplerException {
        Constructor<?>[] constructors = getConstructors(token);
        if (constructors.length == 0) {
            throw new ImplerException("Class has no public constructors");
        }
        for (Constructor<?> constructor : constructors) {
            writer.write(CONSTRUCTOR_GENERATOR.createExecutable(constructor));
        }
    }

    private static void writeClassHeader(Class<?> token, Writer writer) throws ImplerException, IOException {
        if (Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Invalid class, class is private");
        }
        writer.write(
                new Formatter()
                        .setTextPart(generatePackage(token))
                        .setWord(PUBLIC_NAME)
                        .setWord(generateClassName(token))
                        .setWord(generateExtension(token))
                        .setBlockBeginning(token.getCanonicalName())
                        .getFormattedText()
        );
    }

    private static void writeClassFooter(Class<?> clazz, BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write(new Formatter().setBlockEnding().getFormattedText());
    }

    private static void validateInput(Class<?> token, Path root) throws ImplerException {
        checkObjectIsNull(token, "Token");
        checkObjectIsNull(root, "Class");
        checkTokenForInheritance(token);
    }

    private static void writeClass(Class<?> token, BufferedWriter writer) throws ImplerException, IOException {
        writeClassHeader(token, writer);
        if (!token.isInterface()) {
            writeConstructors(token, writer);
        }
        writeAbstractMethods(token, writer);
        writeClassFooter(token, writer);
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        validateInput(token, root);
        root = getPath(root, token);
        createParent(root);
        try (BufferedWriter writer = Files.newBufferedWriter(root)) {
            writeClass(token, writer);
        } catch (IOException e) {
            throw new ImplerException("Can't write to java file", e);
        }
    }

    private static class CustomMethod {
        private final Method method;

        public Method getMethod() {
            return method;
        }

        CustomMethod(Method method) {
            this.method = method;
        }

        @Override
        public boolean equals(Object object) {
            if (object == null) {
                return false;
            }
            if (object instanceof CustomMethod) {
                CustomMethod another = (CustomMethod) object;
                return method.getName().equals(another.getMethod().getName()) &&
                        Arrays.equals(method.getParameterTypes(),
                                another.getMethod().getParameterTypes());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(Arrays.hashCode(method.getParameterTypes()),
                    method.getName());
        }
    }
}