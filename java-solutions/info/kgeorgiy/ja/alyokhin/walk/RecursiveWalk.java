package info.kgeorgiy.ja.alyokhin.walk;

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
