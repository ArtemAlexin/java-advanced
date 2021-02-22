package info.kgeorgiy.ja.alyokhin.walk;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Stack;

public class Walk extends AbstractWalk{
    @Override
    protected void walkFile(ResultWriter resultWriter, String path) throws ProcessingFileException {
        walkFileWithCondition(resultWriter, path, Files::isDirectory);
    }
    public static void main(String[] args) {
        Walk solver = new Walk();
        solver.run(args);
    }
}
