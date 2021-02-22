package info.kgeorgiy.ja.alyokhin.walk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.function.Predicate;

public abstract class AbstractWalk {
    protected void run(final String[] args) {
        try {
            if (!validArgs(args)) {
                printError("Exactly two arguments must be passed to the input");
                return;
            }
            final Path inputFile = Path.of(args[0]);
            final Path outputFile = Path.of(args[1]);
            createDirectory(outputFile);
            processData(inputFile, outputFile);
        } catch (final InvalidPathException e) {
            // :NOTE: Системные сообщения?
            // :NOTE: Какой путь неправильный?
            printError("Invalid path is passed to the input");
        } catch (final ProcessingFileException e) {
            printError(e.getMessage());
        }
    }

    private void printError(final String message) {
        System.out.println(message);
    }

    private boolean validArgs(final String[] args) {
        return args != null && args.length == 2 && args[0] != null && args[1] != null;
    }

    private void createDirectory(final Path outputFile) throws ProcessingFileException {
        final Path parent = outputFile.getParent();
        if (parent != null && Files.notExists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (final IOException e) {
                throw new ProcessingFileException("Can not create file: [" + outputFile + "]");
            }
        }
    }

    protected void walkFileWithCondition(final ResultWriter resultWriter, final String path, final Predicate<Path> failCondition) throws ProcessingFileException {
        try {
            final Path file = Path.of(path);
            try {
                if (failCondition.test(file)) {
                    resultWriter.writeErrorResult(path);
                } else {
                    // :NOTE: Новый Visitor
                    Files.walkFileTree(file, new CustomFileVisitor(resultWriter, this::calculateHash));
                }
            } catch (final IOException e) {
                throw new ProcessingFileException("Error during walking the tree of file: [" + file + "]");
            }
        } catch (final InvalidPathException e) {
            resultWriter.writeErrorResult(path);
        }
    }

    protected abstract void walkFile(ResultWriter resultWriter, String path) throws ProcessingFileException;

    private void processData(final Path input, final Path output) throws ProcessingFileException {
        // :NOTE: Переставить
        try (final ResultWriter resultWriter = new ResultWriter(output)) {
            try (final BufferedReader reader = Files.newBufferedReader(input)) {
                String readData;
                while ((readData = reader.readLine()) != null) {
                    walkFile(resultWriter, readData);
                }
            } catch (final ProcessingFileException e) {
                throw e;
            } catch (final IOException e) {
                // :NOTE: Системные сообщения?
                throw new ProcessingFileException("Can not process file: [" + input.toString() + "]");
            }
        } catch (final IOException e) {
            throw new ProcessingFileException("Can not process file: [" + output.toString() + "]");
        }
    }

    private long calculateHash(final Path path) {
        try (final InputStream input = Files.newInputStream(path)) {
            long hash = 0;
            int readCount;
            final byte[] b = new byte[1024];
            while ((readCount = input.read(b, 0, b.length)) >= 0) {
                for (int i = 0; i < readCount; i++) {
                    hash = (hash << 8) + (b[i] & 0xff);
                    final long high = hash & 0xff00_0000_0000_0000L;
                    if (high != 0) {
                        hash ^= high >> 48;
                        hash &= ~high;
                    }
                }
            }
            return hash;
        } catch (final IOException e) {
            return 0;
        }
    }
}
