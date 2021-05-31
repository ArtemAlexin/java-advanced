package info.kgeorgiy.ja.alyokhin.i18n.parsers;

import info.kgeorgiy.ja.alyokhin.i18n.parsers.AbstractParser;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

public class MoneyParser extends AbstractParser<Number> {
    public MoneyParser(Locale locale) {
        super(locale);
    }

    @Override
    public Number parse(String text, ParsePosition position) {
        final NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        return format.parse(text, position);
    }
}
