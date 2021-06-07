package info.kgeorgiy.ja.alyokhin.i18n.collectors;

import info.kgeorgiy.ja.alyokhin.i18n.StatisticsData;

import java.text.BreakIterator;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;

public abstract class AbstractStatCollector<T> {
    protected Locale locale;
    protected String text;

    public AbstractStatCollector(Locale locale, String text) {
        this.locale = locale;
        this.text = text;
    }

    protected List<T> commonCollect(final String text, final Locale locale,
                                    final BiFunction<String, ParsePosition, T> parser) {
        final BreakIterator breakIterator = BreakIterator.getWordInstance(locale);
        breakIterator.setText(text);
        final List<T> samples = new ArrayList<>();

        for (int start = breakIterator.first(), end = breakIterator.next(), ignoreLimit = 0;
             end != BreakIterator.DONE;
             start = end, end = breakIterator.next()) {
            if (start < ignoreLimit) {
                continue;
            }
            final ParsePosition position = new ParsePosition(start);
            final T sample = parser.apply(text, position);
            if (sample != null) {
                samples.add(sample);
                ignoreLimit = position.getIndex();
            }
        }

        return samples;
    }

    public abstract StatisticsData<T> collect();
}
