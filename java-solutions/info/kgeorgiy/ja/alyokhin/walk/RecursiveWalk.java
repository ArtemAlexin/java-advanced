package info.kgeorgiy.java.advanced.alyokhin.walk;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RecursiveWalk extends AbstractWalk {
    public static void main(String[] args) {
        RecursiveWalk solver = new RecursiveWalk();
        solver.run(args);
    }

    @Override
    protected void walkFile(ResultWriter resultWriter, String path) throws HandlingFileException {
        walkFileWithCondition(resultWriter, path, x -> false);
    }
}
