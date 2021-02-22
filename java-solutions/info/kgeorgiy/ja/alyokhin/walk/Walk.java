package info.kgeorgiy.ja.alyokhin.walk;

import java.nio.file.Files;

public class Walk extends AbstractWalk{
    @Override
    protected void walkFile(final ResultWriter resultWriter, final String path) throws ProcessingFileException {
        walkFileWithCondition(resultWriter, path, Files::isDirectory);
    }
    public static void main(final String[] args) {
        new Walk().run(args);
    }
}
