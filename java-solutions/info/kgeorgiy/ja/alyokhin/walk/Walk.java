package info.kgeorgiy.ja.alyokhin.walk;

import java.nio.file.Files;

public class Walk extends AbstractWalk{
    @Override
    protected void walkFile(ResultWriter resultWriter, String path) throws HandlingFileException {
        walkFileWithCondition(resultWriter, path, Files::isDirectory);
    }
    public static void main(String[] args) {
        Walk solver = new Walk();
        solver.run(args);
    }
}
