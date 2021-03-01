package info.kgeorgiy.ja.alyokhin.walk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.function.Predicate;

public abstract class AbstractWalk {
    private final CustomFileVisitor fileVisitor = new CustomFileVisitor(this::calculateHash);

    private Path processFileName(final String fileName) throws ProcessingFileException {
        try {
            return Path.of(fileName);
        } catch (final InvalidPathException e) {
            throw new ProcessingFileException("Invalid path is passed to the input + [" + fileName + "]", e);
        }
    }

    protected void run(final String[] args) {
        try {
            if (!validArgs(args)) {
                System.out.println("Exactly two arguments must be passed to the input");
                return;
            }

            final Path inputFile = processFileName(args[0]);
            final Path outputFile = processFileName(args[1]);
            createDirectory(outputFile);
            processData(inputFile, outputFile);
        } catch (final ProcessingFileException e) {
            System.err.println(e.getMessage());
        }
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
                throw new ProcessingFileException("Can not create file: [" + outputFile + "]", e);
            }
        }
    }

    protected void walkFileWithCondition(final String path, final Predicate<Path> failCondition) throws ProcessingFileException {
        try {
            final Path file = Path.of(path);
            try {
                if (failCondition.test(file)) {
                    fileVisitor.visitFailed(path);
                } else {
                    Files.walkFileTree(file, fileVisitor);
                }
            } catch (final IOException e) {
                throw new ProcessingFileException("Error during walking the tree of file: [" + file + "]", e);
            }
        } catch (final InvalidPathException e) {
            fileVisitor.visitFailed(path);
        }
    }

    protected abstract void walkFile(String path) throws ProcessingFileException;

    private void processData(final Path input, final Path output) throws ProcessingFileException {
        try (final BufferedReader reader = Files.newBufferedReader(input)) {
            try (final ResultWriter resultWriter = new ResultWriter(output)) {
                fileVisitor.setResultWriter(resultWriter);
                String readData;
                // :NOTE: Сообщение
                while ((readData = reader.readLine()) != null) {
                    walkFile(readData);
                }
            } catch (final ProcessingFileException e) {
                throw e;
            } catch (final IOException e) {
                throw new ProcessingFileException("Can not process file: [" + output.toString() + "]", e);
            }
        } catch (final IOException e) {
            throw new ProcessingFileException("Can not process file: [" + input.toString() + "]", e);
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
