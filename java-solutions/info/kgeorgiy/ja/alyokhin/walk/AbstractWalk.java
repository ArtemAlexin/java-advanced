package info.kgeorgiy.ja.alyokhin.walk;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.function.Predicate;

public abstract class AbstractWalk {
    protected void run(String[] args) {
        try {
            Path inputFile = Path.of(args[0]);
            Path outputFile = Path.of(args[1]);
            createDirectory(outputFile);
            handleData(inputFile, outputFile);
        } catch (RuntimeException e) {
            System.out.println("Invalid arguments");
        } catch (HandlingFileException e) {
            System.out.println(e.getMessage());
        }
    }

    private void createDirectory(Path outputFile) throws HandlingFileException {
        Path parent = outputFile.getParent();
        if (parent != null && Files.notExists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new HandlingFileException("Can not create file: [" + outputFile + "]");
            }
        }
    }

    protected void walkFileWithCondition(ResultWriter resultWriter, String path, Predicate<Path> failCondition) throws HandlingFileException {
        try {
            Path file = Path.of(path);
            try {
                if (failCondition.test(file)) {
                    resultWriter.writeErrorResult(path);
                } else {
                    Files.walkFileTree(file, new CustomFileVisitor(resultWriter));

                }
            } catch (IOException e) {
                throw new HandlingFileException("Error during walking the tree of file: [" + file + "]");
            }
        } catch (InvalidPathException e) {
            resultWriter.writeErrorResult(path);
        }
    }

    protected abstract void walkFile(ResultWriter resultWriter, String path) throws HandlingFileException;

    private void handleData(Path input, Path output) throws HandlingFileException {
        try (ResultWriter resultWriter = new ResultWriter(output)) {
            try (BufferedReader reader = Files.newBufferedReader(input)) {
                String readData;
                while ((readData = reader.readLine()) != null) {
                    walkFile(resultWriter, readData);
                }
            } catch (HandlingFileException e) {
                throw e;
            } catch (IOException e) {
                throw new HandlingFileException("Can not handle file: [" + input.toString() + "]");
            }
        } catch (IOException e) {
            throw new HandlingFileException("Can not handle file: [" + output.toString() + "]");
        }
    }
}
