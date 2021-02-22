package info.kgeorgiy.ja.alyokhin.walk;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Function;

public class CustomFileVisitor extends SimpleFileVisitor<Path> {
    private final ResultWriter resultWriter;
    private final Function<Path, Long> hashFunction;
    public CustomFileVisitor(ResultWriter resultWriter, Function<Path, Long> hashFunction) {
        this.resultWriter = resultWriter;
        this.hashFunction = hashFunction;
    }
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        resultWriter.writeResult(file, hashFunction.apply(file));
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        resultWriter.writeErrorResult(file);
        return FileVisitResult.CONTINUE;
    }
}
