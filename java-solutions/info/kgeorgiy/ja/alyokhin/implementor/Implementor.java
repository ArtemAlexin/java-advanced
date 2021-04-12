package info.kgeorgiy.ja.alyokhin.implementor;

import info.kgeorgiy.ja.alyokhin.implementor.generic.GenericTypeGeneratorUtils;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import static info.kgeorgiy.ja.alyokhin.implementor.Utils.*;

/**
 * Implementation of {@link JarImpler} interface
 */
public class Implementor implements JarImpler {
    /**
     * Instance of {@link MethodGenerator} which creates implementation of methods.
     * Used in {@link #writeAbstractMethods}.
     */
    private static final MethodGenerator METHOD_GENERATOR = new MethodGenerator();
    /**
     * Instance of {@link ConstructorGenerator} which creates implementation of methods.
     * Used in {@link #writeConstructors}.
     */
    private static final ConstructorGenerator CONSTRUCTOR_GENERATOR = new ConstructorGenerator(Implementor::generateClassName);
    /**
     * Instance of {@link VisitorCleaner}.
     * Used in {@link #deleteTmpDirectory}.
     */
    private static final FileVisitor<Path> FILE_VISITOR = new VisitorCleaner();

    /**
     * Returns package name of the given class.
     *
     * @param token type token to get package.
     * @return if token is null, then empty string is returned, package name of package otherwise.
     */
    private static String getPackageName(Class<?> token) {
        return Objects.isNull(token) || token.getPackageName().startsWith("java.") ? "" : token.getPackageName();
    }

    /**
     * Returns name of the given class with given extension.
     * Name of the class is gotten using {@link Class#getSimpleName()}.
     * Example {@code generateClassWithName(String.class, "Impl.java")} will return {@code StringImpl.java}.
     *
     * @param token     type token for class.
     * @param extension {@code String} representing the extension..
     * @return name with extension.
     * @throws NullPointerException if <code>token</code> is null.
     */
    private static String generateClassWithName(Class<?> token, String extension) {
        return token.getSimpleName() + extension;
    }

    /**
     * Is equivalent of {@code generateClassWithName(token, "Impl.java")}.
     *
     * @param token type according to which name must be generated.
     * @return name with extension {@code "Impl.java}.
     * @see #generateClassWithName
     */
    private static String getClassNameImpl(Class<?> token) {
        return generateClassWithName(token, "Impl.java");
    }

    /**
     * Is equivalent of {@code generateClassWithName(token, "Impl.class")}.
     *
     * @param token type according to which name must be generated.
     * @return name with extension {@code "Impl.class"}.
     * @see #generateClassWithName
     */
    private static String getCompiledClassName(Class<?> token) {
        return generateClassWithName(token, "Impl.class");
    }

    /**
     * Is equivalent of {@code generateClassWithName(token, "Impl")}.
     *
     * @param token type according to which name must be generated.
     * @return name with extension {@code "Impl"}.
     * @see #generateClassWithName
     */
    private static String generateClassName(Class<?> token) {
        return generateClassWithName(token, "Impl");
    }

    /**
     * Returns result of applying <var>function</var> to given <var>token</var>, resolved against given <var>path</var>.
     * Invokes {@link Path#resolve}.
     *
     * @param path     {@code Path} against which resulting path will be resolved.
     * @param token    type token representing class.
     * @param function {@code Function} to be applied to the given type token.
     * @return {@code Path} object representing described path.
     * @throws NullPointerException if <var>path</var> is null.
     */
    private static Path getPath(Path path, Class<?> token, Function<Class<?>, String> function) {
        return path.resolve(getPackageName(token).
                replace('.', File.separatorChar)).
                resolve(function.apply(token));
    }

    /**
     * Is equivalent of {@code getPath(path, token, Implementor::getClassNameImpl)}.
     *
     * @param path  Path againts which result must be resolved.
     * @param token type token representing class.
     * @return {@code Path} object representing described path
     * and resolved against result of applying {@link Implementor#getClassNameImpl}
     * @see #getPath
     */
    private static Path getPath(Path path, Class<?> token) {
        return getPath(path, token, Implementor::getClassNameImpl);
    }

    /**
     * Is equivalent of {@code getPath(Path.of(""), token, classNameGenerator)}.
     *
     * @param token              type token representing class.
     * @param classNameGenerator {@code Function} to generate name by type token.
     * @return {@code String} representing relative path to the
     * <var>token</var> with applied <var>classNameGenerator</var>.
     * @see #getPath
     */
    static String getFilePath(Class<?> token, Function<Class<?>, String> classNameGenerator) {
        return getPath(Path.of(""), token, classNameGenerator).toString();
    }

    /**
     * Returns {@code String} representing package of given {@code token}.
     * Example {@code generatePackage(String.class)} return {@code "package java.lang"}.
     *
     * @param token type token to get package for.
     * @return package.
     */
    private static String generatePackage(Class<?> token) {
        Formatter formatter = new Formatter();
        if (!getPackageName(token).isEmpty()) {
            formatter.setWord(PACKAGE_PREFIX).setStatement(getPackageName(token));
        }
        return formatter.setLineEnding().getFormattedText();
    }

    /**
     * Return {@code String} representing extension to inherit or implement this class or interface.
     *
     * @param token type token of the class.
     * @return {@code "implements"} if token is interface, {@code "extends"} otherwise.
     */
    private static String generateExtension(Class<?> token) {
        return token.isInterface() ? IMPLEMENTS_STATEMENT : EXTENDS_STATEMENT;
    }

    /**
     * Checks that given <var>obj</var> is null.
     * Example {@code checkObjectIsNull(null, "Example")} will throw exception with message @{code "Example is null"}.
     *
     * @param obj  {@code Object} to be checked.
     * @param name is name of object which will be reflected in exception message, if {@code obj} is null.
     * @throws ImplerException if <var>obj</var> is null.
     */
    private static void checkObjectIsNull(Object obj, String name) throws ImplerException {
        if (Objects.isNull(obj)) {
            throw new ImplerException(name + " is null");
        }
    }

    /**
     * Checks that it is possible to inherit from class(or implement interface)
     * representing given {@code token}.
     *
     * @param token type token of the class.
     * @throws ImplerException      if it is not possible to inherit(implement) because of:
     *                              <ul>
     *                                  <li>Given {@code Class} represents an array or primitive</li>
     *                                  <li>Given {@code Class} is final or private</li>
     *                                  <li>Givem {@code Class} equals to {@code Enum.class}</li>
     *                              </ul>
     * @throws NullPointerException if token is @{code null}.
     */
    private static void checkTokenForInheritance(Class<?> token) throws ImplerException {
        if (token.isPrimitive()
                || token.isArray()
                || Modifier.isFinal(token.getModifiers())
                || Modifier.isPrivate(token.getModifiers())
                || token == Enum.class) {
            throw new ImplerException("Unable to implement this class");
        }
    }

    /**
     * Create parent directory for path represented by @{code root}.
     * Invokes {@link Files#createDirectories}.
     *
     * @param root {@code Path} which parent will be created.
     * @throws ImplerException if an {@link IOException} occurred.
     */
    private static void createParent(Path root) throws ImplerException {
        if (root.getParent() != null) {
            try {
                Files.createDirectories(root.getParent());
            } catch (IOException e) {
                throw new ImplerException("Unable to create directories to file", e);
            }
        }
    }

    /**
     * Add all {@link Method} from <var>methods</var> to the <var>set</var> using <var>predicate</var>.
     * All methods are cast to {@link CustomMethod} using {@link CustomMethod#CustomMethod}.
     * Methods which are marked as bridge will not be added to <var>set</var>.
     *
     * @param methods   array of methods to be added to <var>set</var>
     * @param set       resulting {@code Set}, where all <var>methods</var> will be added.
     * @param predicate {@code Predicate} which will be used to filter methods.
     */
    private static void addMethods(Method[] methods, Set<CustomMethod> set, Predicate<Method> predicate) {
        Arrays.stream(methods).
                filter(predicate.and(Predicate.not(Method::isBridge))).
                map(CustomMethod::new).
                collect(Collectors.toCollection(() -> set));
    }

    /**
     * Returns {@code Set} of all not public methods of given {@code Class} and all it's superclasses filtering with predicate.
     * All methods are mapped to {@link CustomMethod} using {@link CustomMethod#CustomMethod}.
     * Invokes {@link Class#getDeclaredMethods()}, {@link Class#getSuperclass()} and {@link #addMethods}.
     *
     * @param token     type token representing class.
     * @param predicate {@code Predicate} to filter methods.
     * @return {@code Set} of methods.
     */
    private static Set<CustomMethod> getAllMethodWithPredicate(Class<?> token, Predicate<Method> predicate) {
        Set<CustomMethod> methods = new HashSet<>();
        addMethods(token.getMethods(), methods, predicate);
        while (token != null) {
            addMethods(token.getDeclaredMethods(), methods, predicate);
            token = token.getSuperclass();
        }
        return methods;
    }

    /**
     * Returns an array of non-private {@link Constructor} of the class represented by <var>token</var>.
     *
     * @param token {@code Class} where constructors will be searched.
     * @return array of constructors.
     */
    private static Constructor<?>[] getConstructors(Class<?> token) {
        return Arrays.stream(token.getDeclaredConstructors()).
                filter(constructor -> !Modifier.isPrivate(constructor.getModifiers())).
                toArray(Constructor[]::new);
    }

    /**
     * Writes {@link String} implementation  of all methods which must be implemented to the given {@link BufferedWriter}.
     * Uses {@link Implementor#METHOD_GENERATOR} to create implementation of methods.
     * Invokes {@link AbstractExecutableGenerator#createExecutable} to create {@code String representation} of method.
     *
     * @param token  type token which represents class or interface that must be implemented.
     * @param writer {@code BufferedWriter} where.
     * @throws IOException if {@link BufferedWriter#write(String)} throws an {@code IOException}.
     */
    private static void writeAbstractMethods(Class<?> token, UnicodeBufferedWriter writer) throws IOException {
        Set<CustomMethod> methods = getAllMethodWithPredicate(token,
                method -> Modifier.isAbstract(method.getModifiers()));
        methods.removeAll(getAllMethodWithPredicate(token,
                method -> Modifier.isFinal(method.getModifiers())));
        for (CustomMethod method : methods) {
            writer.write(METHOD_GENERATOR.createExecutable(method.getMethod()));
        }
    }

    /**
     * Writes {@link String} implementation of all constructors which must be implemented to the given {@link UnicodeBufferedWriter}.
     * Invokes {@link AbstractExecutableGenerator#createExecutable} to create {@code String representation} of method.
     * Uses {@link Implementor#CONSTRUCTOR_GENERATOR} to create constructor implementation.
     *
     * @param token  type token which represents class or interface that must be implemented.
     * @param writer {@code UnicodeBufferedWriter} where.
     * @throws IOException     if {@link UnicodeBufferedWriter#write} throws an {@code IOException}.
     * @throws ImplerException if <var>token</var> has no public constructors.
     */
    private static void writeConstructors(Class<?> token, UnicodeBufferedWriter writer) throws IOException, ImplerException {
        Constructor<?>[] constructors = getConstructors(token);
        if (constructors.length == 0) {
            throw new ImplerException("Class has no public constructors");
        }
        for (Constructor<?> constructor : constructors) {
            writer.write(CONSTRUCTOR_GENERATOR.createExecutable(constructor));
        }
    }

    /**
     * Write class header {@link String} representation  of the class which must be implemented to the given {@link UnicodeBufferedWriter}.
     * {@code token} can be both interface or class.
     * Class header is everything till the first left bracket.
     * Example
     * <code>
     * package ru.example.domain
     * public class Example extends AbstractExample implements ExampleInterface {
     * </code>
     *
     * @param token  type token which represents class or interface that must be implemented.
     * @param writer {@code UnicodeBufferedWriter} where generated {@code String} should be written.
     * @throws IOException if {@link UnicodeBufferedWriter#write} throws an {@code IOException}.
     */
    private static void writeClassHeader(Class<?> token, UnicodeBufferedWriter writer) throws IOException {
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

    /**
     * Write class footer {@link String} representation of the class which must be implemented to the given {@link UnicodeBufferedWriter}.
     * {@code token} can be both interface or class.
     * Class footer is {@code "}"} and {@code EOL}.
     *
     * @param bufferedWriter {@link UnicodeBufferedWriter} where implementation must be written.
     * @throws IOException if {@link UnicodeBufferedWriter#write} throws an {@code IOException}.
     */
    private static void writeClassFooter(UnicodeBufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write(new Formatter().setBlockEnding().getFormattedText());
    }

    /**
     * Checks that given {@code token} or {@code root} are valid.
     * Parameters are invalid if at least one of the is null or
     * class(interface) represented by {@code token} can not be inherited(implemented).
     * {@code token} can not be implented if {@link #checkTokenForInheritance} throes an {@code ImplerException}.
     *
     * @param token type token to be checked.
     * @param root  {@code Path} to be checked.
     * @throws ImplerException if ar least one of parameters is invalid.
     */
    private static void validateInput(Class<?> token, Path root) throws ImplerException {
        checkObjectIsNull(token, "Token");
        checkObjectIsNull(root, "Class");
        checkTokenForInheritance(token);
    }

    /**
     * Write {@link String} representation of the class which must be implemented to the given {@link UnicodeBufferedWriter}.
     * {@code token} can be both interface or class.
     * Class representation consists of
     * <ul>
     *      <li>Class header, for more details {@link #writeClassHeader}</li>
     *      <li>Constructors, for more details {@link #writeConstructors}</li>
     *      <li>All methods which must be implemented, for more details {@link #writeAbstractMethods}</li>
     *      <li>Class footer, for more details {@link #writeClassFooter}</li>
     * </ul>
     *
     * @param token  type token which represents class or interface that must be implemented.
     * @param writer {@code UnicodeBufferedWriter} where generated {@code String} should be written.
     * @throws IOException     if {@link UnicodeBufferedWriter#write} throws an {@code IOException}.
     * @throws ImplerException if any of {@link #writeClassHeader}, {@link #writeConstructors},
     *                         {@link #writeAbstractMethods}, {@link #writeClassFooter} throws {@link ImplerException}.
     */
    private static void writeClass(Class<?> token, UnicodeBufferedWriter writer) throws ImplerException, IOException {
        writeClassHeader(token, writer);
        if (!token.isInterface()) {
            writeConstructors(token, writer);
        }
        writeAbstractMethods(token, writer);
        writeClassFooter(writer);
    }

    /**
     * Produces code implementing class or interface specified by provided <var>token</var>.
     * <p>
     * Generated class classes name should be same as classes name of the type token with <var>Impl</var> suffix
     * added. Generated source code should be placed in the correct subdirectory of the specified
     * <var>root</var> directory and have correct file name. For example, the implementation of the
     * interface {@link java.util.List} should go to <var>$root/java/util/ListImpl.java</var>
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws ImplerException if the given class cannot be generated for one of following reasons:
     *                         <ul>
     *                         <li>Given class is null. </li>
     *                         <li> Given class is primitive or array. </li>
     *                         <li> Given class is final class or {@link Enum}. </li>
     *                         <li> class isn't an interface and contains only private constructors.</li>
     *                         <li> The problems with I/O occurred during writing implementation. </li>
     *                         </ul>
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        validateInput(token, root);
        root = getPath(root, token);
        createParent(root);
        try (BufferedWriter writer = Files.newBufferedWriter(root)) {
            writeClass(token, new UnicodeBufferedWriter(writer));
        } catch (IOException e) {
            throw new ImplerException("Can't write to java file", e);
        }
    }


    /**
     * Creates .jar file implementing class or interface specified by provided class.
     * <p>
     * During implementation creates temporary folder to store temporary .java and .class files.
     * If program fails to delete temporary folder, it provides information about it.
     *
     * @throws ImplerException if the given class cannot be generated for one of following reasons:
     *                         <ul>
     *                         <li> <var>token</var> is null</li>
     *                         <li> Error occurs during implementation using {@link #implement(Class, Path)} </li>
     *                         <li> {@link JavaCompiler} failed to compile implemented class </li>
     *                         <li> The problems with I/O occurred during writing implementation. </li>
     *                         </ul>
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        validateInput(token, jarFile);
        createParent(jarFile);
        Path parentDirectory = jarFile.getParent() == null ? Path.of("") : jarFile.getParent();
        Path tmpDirectory = parentDirectory.resolve(FOLDER_TO_COMPILE).resolve(TMP_FOLDER);
        try {
            implement(token, tmpDirectory);
            compile(token, tmpDirectory);
            build(token, tmpDirectory, jarFile);
        } finally {
            deleteTmpDirectory(tmpDirectory.getParent());
        }
    }

    /**
     * Recursively delete all directories and files in path represented by <var>tmpDirectory</var>}.
     * Uses {@link Implementor#FILE_VISITOR} and invokes {@link Files#walkFileTree} to clean file tree.
     *
     * @param tmpDirectory {@code Path} to the root of file tree to be deleted.
     * @throws ImplerException if {@link Files#walkFileTree} throws {@link IOException}.
     */
    private void deleteTmpDirectory(Path tmpDirectory) throws ImplerException {
        try {
            Files.walkFileTree(tmpDirectory, FILE_VISITOR);
        } catch (IOException e) {
            throw new ImplerException("Error while deleting temporary folder occurred");
        }
    }

    /**
     * Produces <code>.jar</code> file using compiled classes, generates manifest file.
     *
     * @param token        type token which represents class or interface that must be implemented.
     * @param tmpDirectory directory containing all <code>.class</code> files.
     * @param jarFile      {@code Path} for resulting <code>.jar</code> file.
     * @throws ImplerException if {@link JarOutputStream} throws {@link IOException}.
     */
    private void build(Class<?> token, Path tmpDirectory, Path jarFile) throws ImplerException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream outputStream = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            outputStream.putNextEntry(new ZipEntry(
                    getNameForJar(token)));
            System.err.println(getPath(tmpDirectory, token, Implementor::getCompiledClassName));
            Files.copy(getPath(tmpDirectory, token, Implementor::getCompiledClassName), outputStream);
        } catch (IOException e) {
            throw new ImplerException("Unable to write to Jar file");
        }
    }

    /**
     * Returns {@code String} representation of relative path to the class provided by <var>token</var>.
     * {@link Utils#JarFileSeparator} is used as a delimiter.
     *
     * @param token type token representing class.
     * @return relative path to the class.
     */
    private static String getNameForJar(Class<?> token) {
        return String.join(JarFileSeparator, getPackageName(token).split("\\.")) +
                JarFileSeparator + getCompiledClassName(token);
    }

    /**
     * Compiles {@code .java} source code which implements class represented by <var>token</var>.
     * Compiled files are placed in the same directory.
     *
     * @param token        type token which represents class or interface that must be implemented.
     * @param tmpDirectory {@link Path} where package with source code is placed.
     * @throws ImplerException if source code can not be compiled.
     */
    private void compile(Class<?> token, Path tmpDirectory) throws ImplerException {
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        validateCompiler(javaCompiler);
        URI classpath;
        String[] args;
        try {
            var source = token.getProtectionDomain().getCodeSource();
            var path = tmpDirectory.resolve(getFilePath(token, Implementor::getClassNameImpl)).toString();
            if (Objects.isNull(source)) {
                args = new String[]{path};
            } else {
                classpath = source.getLocation().toURI();
                args = new String[]{
                        "-cp",
                        Path.of(classpath).toString(),
                        tmpDirectory.resolve(getFilePath(token, Implementor::getClassNameImpl)).toString()};
            }
        } catch (
                URISyntaxException e) {
            throw new ImplerException("Failed to compile class, can not convert URL to URI");
        }
        if (javaCompiler.run(null, null, null, args) != 0) {
            throw new ImplerException("Error during compiling class");
        }
    }

    /**
     * Check that given <var>javaCompiler</var> is not {@code null}.
     *
     * @param javaCompiler {@link JavaCompiler} object to be checked.
     * @throws ImplerException if <var>javaCompiler</var> is {@code null}.
     */
    private void validateCompiler(JavaCompiler javaCompiler) throws ImplerException {
        checkObjectIsNull(javaCompiler, "Java compiler can not be found");
    }

    /**
     * Wraps {@link Method} object and provide different {@link Object#equals}, {@link Object#hashCode()} methods.
     */
    private static class CustomMethod {
        /**
         * Instance of {@link Method} which is wrapped.
         */
        private final Method method;

        /**
         * Returns encapsulated method.
         *
         * @return {@link Method} object
         */
        public Method getMethod() {
            return method;
        }

        /**
         * Constructor with 1 argument.
         *
         * @param method {@link Method} to wrap.
         */
        CustomMethod(Method method) {
            this.method = method;
        }

        /**
         * Compare {@link CustomMethod} instances.
         * If provided <var>object</var> is not instance of {@code CustomMethod} then result is {@code false}.
         * If wrapped methods have equal names and parameter types then result {@code true}.
         *
         * @param object {@link Object} to compare.
         * @return true, @code true} if the objects are the same; {@code false}
         * otherwise.
         */
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

        /**
         * Returns hash value {@link CustomMethod}.
         * Hash is computed as sum of hashes of wrapped {@link Method} parameter types.
         * and name. Hash for parameter types is computed as {@link Arrays#hashCode}.
         *
         * @return hash value for this object.
         */
        @Override
        public int hashCode() {
            return Objects.hash(Arrays.hashCode(method.getParameterTypes()),
                    method.getName());
        }

    }

    /**
     * Invokes {@link Class#forName} for given class.
     *
     * @param className name of the desired class.
     * @return {@code Class} object for the given class.
     * @throws ImplerException if the class cannot be located.
     */
    private static Class<?> classFromString(String className) throws ImplerException {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ImplerException("Invalid class name: " + className, e);
        }
    }

    /**
     * Invokes {@link Path#of} for given sequence of strings.
     *
     * @param first starting part of desired path.
     * @param more  additional strings to be joined for the path.
     * @return resulting {@code Path}.
     * @throws ImplerException if the path string cannot be converted to a {@link Path}.
     */
    private static Path pathFromString(String first, String... more) throws ImplerException {
        try {
            return Path.of(first, more);
        } catch (InvalidPathException e) {
            throw new ImplerException("Invalid path: " + first + "...", e);
        }
    }

    /**
     * Gets console arguments and invokes necessary implementation.
     * <ul>
     *     <li>2 arguments <code>className rootPath</code>:
     *     creates <code>.java</code> file by invoking {@link #implement}</li>
     *     <li>3 arguments <code>-jar className jarPath</code>:
     *     creates <code>.jar</code> file by invoking {@link #implementJar}</li>
     * </ul>
     *
     * @param args arguments for the application.
     */
    public static void main(String[] args) {
        try {
            if (args == null || args.length < 2 || args.length > 3) {
                throw new ImplerException("Invalid arguments, expected: [-jar] <class.name> <output.file>.");
            }
            for (String arg : args) {
                if (arg == null) {
                    throw new ImplerException("Invalid arguments, expected non-null.");
                }
            }
            if (args.length == 3 && !args[0].equals("-jar")) {
                throw new ImplerException("Invalid arguments, expected: [-jar] <class.name> <output.file>.");
            }

            Implementor generator = new Implementor();
            if (args.length == 2) {
                generator.implement(classFromString(args[0]), pathFromString(args[1]));
            } else {
                generator.implementJar(classFromString(args[1]), pathFromString(args[2]));
            }
        } catch (ImplerException | NullPointerException e) {
            System.err.println(e.getMessage());
        }
    }
}