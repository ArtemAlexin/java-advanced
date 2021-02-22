package info.kgeorgiy.ja.alyokhin.walk;

public class RecursiveWalk extends AbstractWalk {
    public static void main(String[] args) {
        new RecursiveWalk().run(args);
    }
    @Override
    protected void walkFile(ResultWriter resultWriter, String path) throws ProcessingFileException {
        walkFileWithCondition(resultWriter, path, x -> false);
    }
}
