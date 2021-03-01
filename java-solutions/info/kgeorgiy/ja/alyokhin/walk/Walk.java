package info.kgeorgiy.ja.alyokhin.walk;

import java.nio.file.Files;

public class Walk extends AbstractWalk{
    @Override
    protected void walkFile(final String path) throws ProcessingFileException {
        walkFileWithCondition(path, Files::isDirectory);
    }

    public static void main(final String[] args) {
        new Walk().run(args);
    }
}
