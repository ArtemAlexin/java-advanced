package info.kgeorgiy.ja.alyokhin.walk;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

public abstract class AbstractWalk {
    protected void run(final String[] args) {
        try {
            final Path inputFile = Path.of(args[0]);
            final Path outputFile = Path.of(args[1]);
            createDirectory(outputFile);
            handleData(inputFile, outputFile);
            // :NOTE: catch (RuntimeException)
        } catch (final RuntimeException e) {
            System.out.println("Invalid arguments");
        } catch (final HandlingFileException e) {
            System.out.println(e.getMessage());
        }
    }

    private void createDirectory(final Path outputFile) throws HandlingFileException {
        final Path parent = outputFile.getParent();
        if (parent != null && Files.notExists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (final IOException e) {
                throw new HandlingFileException("Can not create file: [" + outputFile + "]");
            }
        }
    }

    protected void walkFileWithCondition(final ResultWriter resultWriter, final String path, final Predicate<Path> failCondition) throws HandlingFileException {
        try {
            final Path file = Path.of(path);
            try {
                if (failCondition.test(file)) {
                    resultWriter.writeErrorResult(path);
                } else {
                    Files.walkFileTree(file, new CustomFileVisitor(resultWriter));

                }
            } catch (final IOException e) {
                throw new HandlingFileException("Error during walking the tree of file: [" + file + "]");
            }
        } catch (final RuntimeException e) {
            resultWriter.writeErrorResult(path);
        }
    }

    protected abstract void walkFile(ResultWriter resultWriter, String path) throws HandlingFileException;

    private void handleData(final Path input, final Path output) throws HandlingFileException {
        try (final ResultWriter resultWriter = new ResultWriter(output)) {
            try (final BufferedReader reader = Files.newBufferedReader(input)) {
                String readData;
                while ((readData = reader.readLine()) != null) {
                    walkFile(resultWriter, readData);
                }
            } catch (final HandlingFileException e) {
                throw e;
            } catch (final IOException e) {
                throw new HandlingFileException("Can not handle file: [" + input.toString() + "]");
            }
        } catch (final IOException e) {
            throw new HandlingFileException("Can not handle file: [" + output.toString() + "]");
        }
    }
}
