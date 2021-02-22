package info.kgeorgiy.ja.alyokhin.walk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.function.Predicate;

public abstract class AbstractWalk {
    protected void run(String[] args) {
        try {
            if (!validArgs(args)) {
                printError("Exactly two arguments must be passed to the input");
                return;
            }
            Path inputFile = Path.of(args[0]);
            Path outputFile = Path.of(args[1]);
            createDirectory(outputFile);
            processData(inputFile, outputFile);
        } catch (InvalidPathException e) {
            printError("Invalid path is passed to the input");
        } catch (ProcessingFileException e) {
            printError(e.getMessage());
        }
    }

    private void printError(String message) {
        System.out.println(message);
    }

    private boolean validArgs(String[] args) {
        return args != null && args.length == 2 && args[0] != null && args[1] != null;
    }

    private void createDirectory(Path outputFile) throws ProcessingFileException {
        Path parent = outputFile.getParent();
        if (parent != null && Files.notExists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new ProcessingFileException("Can not create file: [" + outputFile + "]");
            }
        }
    }

    protected void walkFileWithCondition(ResultWriter resultWriter, String path, Predicate<Path> failCondition) throws ProcessingFileException {
        try {
            Path file = Path.of(path);
            try {
                if (failCondition.test(file)) {
                    resultWriter.writeErrorResult(path);
                } else {
                    Files.walkFileTree(file, new CustomFileVisitor(resultWriter, this::calculateHash));
                }
            } catch (IOException e) {
                throw new ProcessingFileException("Error during walking the tree of file: [" + file + "]");
            }
        } catch (InvalidPathException e) {
            resultWriter.writeErrorResult(path);
        }
    }

    protected abstract void walkFile(ResultWriter resultWriter, String path) throws ProcessingFileException;

    private void processData(Path input, Path output) throws ProcessingFileException {
        try (ResultWriter resultWriter = new ResultWriter(output)) {
            try (BufferedReader reader = Files.newBufferedReader(input)) {
                String readData;
                while ((readData = reader.readLine()) != null) {
                    walkFile(resultWriter, readData);
                }
            } catch (ProcessingFileException e) {
                throw e;
            } catch (IOException e) {
                throw new ProcessingFileException("Can not process file: [" + input.toString() + "]");
            }
        } catch (IOException e) {
            throw new ProcessingFileException("Can not process file: [" + output.toString() + "]");
        }
    }

    private long calculateHash(Path path) {
        try (InputStream input = Files.newInputStream(path)) {
            long hash = 0;
            int readCount;
            byte[] b = new byte[1024];
            while ((readCount = input.read(b, 0, b.length)) >= 0) {
                for (int i = 0; i < readCount; i++) {
                    hash = (hash << 8) + (b[i] & 0xff);
                    long high = hash & 0xff00_0000_0000_0000L;
                    if (high != 0) {
                        hash ^= high >> 48;
                        hash &= ~high;
                    }
                }
            }
            return hash;
        } catch (IOException e) {
            return 0;
        }
    }
}
