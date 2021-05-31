package info.kgeorgiy.ja.alyokhin.i18n.parsers;

import java.text.ParsePosition;
import java.util.Locale;

public abstract class AbstractParser<T> {
    protected Locale locale;

    public AbstractParser(Locale locale) {
        this.locale = locale;
    }

    public abstract T parse(String text, ParsePosition position);
}
