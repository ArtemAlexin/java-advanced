package info.kgeorgiy.ja.alyokhin.walk;

public class RecursiveWalk extends AbstractWalk {
    public static void main(final String[] args) {
        new RecursiveWalk().run(args);
    }

    @Override
    protected void walkFile(final ResultWriter resultWriter, final String path) throws ProcessingFileException {
        walkFileWithCondition(resultWriter, path, x -> false);
    }
}
