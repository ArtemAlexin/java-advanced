package info.kgeorgiy.ja.alyokhin.implementor;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Successor of {@link SimpleFileVisitor} which cleans created temporary directories and files.
 */
public class VisitorCleaner extends SimpleFileVisitor<Path> {
    /**
     * Deletes file or directory with the given {@link Path};
     * Invokes {@link Files#delete}.
     *
     * @param file path to file or directory which must be deleted.
     * @return {@link FileVisitResult#CONTINUE}.
     * @throws IOException if {@link Files#delete} thrown an error.
     */
    private FileVisitResult visit(Path file) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
    }

    /**
     * Is equivalent of {@code visit(file)}.
     *
     * @see #visit
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        return visit(file);
    }

    /**
     * Is equivalent of {@code visit(dir)}.
     *
     * @see #visit
     */
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return visit(dir);
    }
}
