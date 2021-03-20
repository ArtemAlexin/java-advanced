package info.kgeorgiy.ja.alyokhin.implementor;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static info.kgeorgiy.ja.alyokhin.implementor.Utils.*;

public class Formatter {
    private final StringBuilder stringBuilder = new StringBuilder();

    private Formatter doAction(String... strings) {
        Arrays.asList(strings).forEach(stringBuilder::append);
        return this;
    }

    private Formatter doActionIfPresent(Function<String, Formatter> action, String... strings) {
        Arrays.stream(strings).filter(Predicate.not(String::isEmpty)).forEach(action::apply);
        return this;
    }

    public Formatter setSpace() {
        return setSpace(1);
    }

    public Formatter setSpace(int number) {
        return doAction(SPACE.repeat(number));
    }

    public Formatter setTabulation() {
        return setTabulation(1);
    }

    public Formatter setTabulation(int number) {
        return doAction(TAB.repeat(number));
    }

    public Formatter setTextPart(String text) {
        return doAction(text);
    }

    public Formatter setWord(String word) {
        return doAction(word, SPACE);
    }

    public Formatter setParagraph(String paragraph) {
        return doAction(paragraph, EOL);
    }

    public void newFormatter() {
        stringBuilder.setLength(0);
    }

    public Formatter setStatement(String statement) {
        return doAction(statement, SEMICOLON, EOL);
    }

    public Formatter setWordIfPresent(String word) {
        return doActionIfPresent(this::setWord, word);
    }

    public Formatter setLineEnding() {
        return doAction(EOL);
    }

    public Formatter setBlockBeginning(String block) {
        return doAction(block, SPACE, LBRACKET, EOL);
    }

    public Formatter setBlockEnding(String lastStatement) {
        return doAction(lastStatement, SEMICOLON, EOL, TAB, RBRACKET, EOL);
    }

    public Formatter setBlockEnding() {
        return doAction(RBRACKET, EOL);
    }

    public String getFormattedText() {
        String result = stringBuilder.toString();
        newFormatter();
        return result;
    }
}
