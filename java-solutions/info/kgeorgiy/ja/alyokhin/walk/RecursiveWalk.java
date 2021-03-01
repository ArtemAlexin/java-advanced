package info.kgeorgiy.ja.alyokhin.walk;

public class RecursiveWalk extends AbstractWalk {
    public static void main(final String[] args) {
        new RecursiveWalk().run(args);
    }

    @Override
    protected void walkFile(final String path) throws ProcessingFileException {
        walkFileWithCondition(path, x -> false);
    }
}
