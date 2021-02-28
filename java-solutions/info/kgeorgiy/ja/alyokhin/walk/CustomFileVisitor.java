package info.kgeorgiy.ja.alyokhin.walk;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Function;

public class CustomFileVisitor extends SimpleFileVisitor<Path> {
    private ResultWriter resultWriter;
    private final Function<Path, Long> hashFunction;

    public CustomFileVisitor(final ResultWriter resultWriter, final Function<Path, Long> hashFunction) {
        this.resultWriter = resultWriter;
        this.hashFunction = hashFunction;
    }

    public CustomFileVisitor(Function<Path, Long> hashFunction) {
        this.hashFunction = hashFunction;
    }

    public void setResultWriter(ResultWriter resultWriter) {
        this.resultWriter = resultWriter;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws ProcessingFileException {
        resultWriter.writeResult(file, hashFunction.apply(file));
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws ProcessingFileException {
        resultWriter.writeErrorResult(file);
        return FileVisitResult.CONTINUE;
    }
}
