package info.kgeorgiy.java.advanced.alyokhin.walk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
