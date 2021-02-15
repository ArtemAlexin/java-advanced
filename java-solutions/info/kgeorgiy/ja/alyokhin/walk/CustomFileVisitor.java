package info.kgeorgiy.ja.alyokhin.walk;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class CustomFileVisitor extends SimpleFileVisitor<Path> {
    private final ResultWriter resultWriter;

    public CustomFileVisitor(ResultWriter resultWriter) {
        this.resultWriter = resultWriter;
    }

    private long calculateHash(Path path) {
        try(InputStream input = Files.newInputStream(path)) {
            long hash = 0;
            int readCount;
            byte[] b = new byte[1024];
            while ((readCount = input.read(b, 0, b.length)) >= 0)
            {
                for(int i = 0; i < readCount; i++) {
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
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        resultWriter.writeResult(file, calculateHash(file));
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        resultWriter.writeErrorResult(file);
        return FileVisitResult.CONTINUE;
    }
}
