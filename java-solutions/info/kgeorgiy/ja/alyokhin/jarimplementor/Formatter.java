package info.kgeorgiy.ja.alyokhin.jarimplementor;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

import static info.kgeorgiy.ja.alyokhin.jarimplementor.Utils.*;

/**
 * Class for convenient text formatting.
 * Wraps {@link StringBuilder} instance.
 */
public class Formatter {
    /**
     * Wrapped instance.
     */
    private final StringBuilder stringBuilder = new StringBuilder();

    /**
     * Appends all <var>strings</var> to the text.
     * Invokes {@link StringBuilder#append(String)} for each {@link String} in <var>strings</var>.
     *
     * @param strings vararg of string to be appended.
     * @return a reference to this object.
     */
    private Formatter doAction(String... strings) {
        Arrays.asList(strings).forEach(stringBuilder::append);
        return this;
    }

    /**
     * Apply <var>action</var> for each {@link String} in <var>strings</var> in iteration order.
     *
     * @param strings vararg of string to be appended.
     * @param action  {@link Function} to be applied.
     * @return a reference to this object.
     */
    private Formatter doActionIfPresent(Function<String, Formatter> action, String... strings) {
        Arrays.stream(strings).filter(Predicate.not(String::isEmpty)).forEach(action::apply);
        return this;
    }

    /**
     * Appends one space to the text.
     *
     * @return reference to this.
     */
    public Formatter setSpace() {
        return setSpace(1);
    }

    /**
     * Appends <var>number</var> spaces to the text.
     * Invokes {@link #doAction}.
     *
     * @return reference to this.
     */
    public Formatter setSpace(int number) {
        return doAction(SPACE.repeat(number));
    }

    /**
     * Appends one tabulation to the text.
     * Invokes {@link #setTabulation(int)}.
     *
     * @return reference to this.
     */
    public Formatter setTabulation() {
        return setTabulation(1);
    }

    /**
     * Appends <var>number</var> tabulations to the text.
     * Invokes {@link #doAction}.
     *
     * @return reference to this.
     */
    public Formatter setTabulation(int number) {
        return doAction(TAB.repeat(number));
    }

    /**
     * Appends <var>text</var> to the text.
     * Invokes {@link #doAction}.
     *
     * @return reference to this.
     */
    public Formatter setTextPart(String text) {
        return doAction(text);
    }

    /**
     * Appends one <var>word</var> to the text with {@link Utils#SPACE}.
     * Invokes {@link #doAction}.
     *
     * @return reference to this.
     */
    public Formatter setWord(String word) {
        return doAction(word, SPACE);
    }

    /**
     * Appends one <var>paragraph</var> to the text with {@code EOL}.
     * {@code EOl} is {@link Utils#EOL}.
     * Invokes {@link #doAction}.
     *
     * @return reference to this.
     */
    public Formatter setParagraph(String paragraph) {
        return doAction(paragraph, EOL);
    }

    /**
     * Clears the text that was formatting.
     */
    public void newFormatter() {
        stringBuilder.setLength(0);
    }

    /**
     * Appends <var>statement</var> to the text {@link Utils#SEMICOLON} and {@link Utils#EOL}.
     * Invokes {@link #doAction}.
     *
     * @return reference to this.
     */
    public Formatter setStatement(String statement) {
        return doAction(statement, SEMICOLON, EOL);
    }

    /**
     * Appends <var>word</var> to the text with {@link Utils#SPACE} if word is not empty.
     * Invokes {@link #setWord}, {@link #doActionIfPresent}.
     *
     * @return reference to this.
     */
    public Formatter setWordIfPresent(String word) {
        return doActionIfPresent(this::setWord, word);
    }

    /**
     * Appends {@link Utils#EOL} to the text.
     * Invokes {@link #doAction}.
     *
     * @return reference to this.
     */
    public Formatter setLineEnding() {
        return doAction(EOL);
    }

    /**
     * Appends <var>block</var> to the text {@link Utils#SPACE}, {@link Utils#LBRACKET} and {@link Utils#EOL}.
     * Invokes {@link #doAction}.
     *
     * @return reference to this.
     */
    public Formatter setBlockBeginning(String block) {
        return doAction(block, SPACE, LBRACKET, EOL);
    }

    /**
     * Appends <var>lastStatement</var> to the text {@link Utils#SEMICOLON},
     * {@link Utils#EOL}, {@link Utils#TAB}, {@link Utils#RBRACKET} and {@link Utils#EOL}.
     * Invokes {@link #doAction}
     *
     * @return reference to this.
     */
    public Formatter setBlockEnding(String lastStatement) {
        return doAction(lastStatement, SEMICOLON, EOL, TAB, RBRACKET, EOL);
    }

    /**
     * Appends to the text {@link Utils#RBRACKET} and {@link Utils#EOL}.
     * Invokes {@link #doAction}.
     *
     * @return reference to this.
     */
    public Formatter setBlockEnding() {
        return doAction(RBRACKET, EOL);
    }

    /**
     * Returns {@link String} representing formatted text.
     * Formatter is cleaned using {@link #newFormatter()}.
     * Invokes also {@link #doAction}.
     *
     * @return reference to this.
     */
    public String getFormattedText() {
        String result = stringBuilder.toString();
        newFormatter();
        return result;
    }
}
