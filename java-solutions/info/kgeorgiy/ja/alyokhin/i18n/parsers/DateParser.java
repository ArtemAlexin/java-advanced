package info.kgeorgiy.ja.alyokhin.i18n.parsers;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.*;

public class DateParser extends AbstractParser<Date> {
    public DateParser(Locale locale) {
        super(locale);
    }

    private final DateFormat[] dateFormats = new DateFormat[]{
            DateFormat.getDateInstance(DateFormat.FULL, locale),
            DateFormat.getDateInstance(DateFormat.LONG, locale),
            DateFormat.getDateInstance(DateFormat.MEDIUM, locale),
            DateFormat.getDateInstance(DateFormat.SHORT, locale)
    };

    @Override
    public Date parse(final String text, final ParsePosition position) {
        return Arrays.stream(dateFormats)
                .map(dateFormat -> dateFormat.parse(text, position))
                .filter(Objects::nonNull).findFirst().orElse(null);
    }
}
