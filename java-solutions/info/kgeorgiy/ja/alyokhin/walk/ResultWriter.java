package info.kgeorgiy.ja.alyokhin.walk;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResultWriter implements Closeable {
    private final BufferedWriter writer;
    private final long ERROR_FILE_HASH = 0;
    private final Path fileName;

    public ResultWriter(Path result) throws ProcessingFileException {
        this.fileName = result;
        try {
            writer = Files.newBufferedWriter(result);
        } catch (IOException e) {
            throw new ProcessingFileException("Can not process file:[" + result.toString() + "]");
        }
    }

    public void writeResult(String path, long hash) throws ProcessingFileException {
        String result = String.format("%016x", hash) + " " + path;
        try {
            writer.write(result, 0, result.length());
            writer.newLine();
        } catch (IOException e) {
            throw new ProcessingFileException("Can not write to file: [" + fileName + "]");
        }
    }

    public void writeResult(Path path, long hash) throws ProcessingFileException {
        writeResult(path.toString(), hash);
    }

    public void writeErrorResult(Path path) throws ProcessingFileException {
        writeResult(path, ERROR_FILE_HASH);
    }
    public void writeErrorResult(String path) throws ProcessingFileException {
        writeResult(path, ERROR_FILE_HASH);
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
