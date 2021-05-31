package info.kgeorgiy.ja.alyokhin.i18n.collectors;

import info.kgeorgiy.ja.alyokhin.i18n.StatisticsData;
import info.kgeorgiy.ja.alyokhin.i18n.TextStatistics;
import info.kgeorgiy.ja.alyokhin.i18n.calculators.StringStatisticCalculator;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class StringStatCollector extends AbstractStatCollectors<String> {
    private BreakIterator breakIterator;
    private Predicate<String> filter;
    private TextStatistics.TextType type;

    public StringStatCollector(Locale locale, String text,
                               BreakIterator breakIterator,
                               Predicate<String> filter,
                               TextStatistics.TextType textType) {
        super(locale, text);
        this.breakIterator = breakIterator;
        this.filter = filter;
        this.type = textType;
    }

    @Override
    public StatisticsData<String> collect() {
        breakIterator.setText(text);
        final List<String> samples = new ArrayList<>();
        for (int start = breakIterator.first(), end = breakIterator.next(); end != BreakIterator.DONE;
             start = end, end = breakIterator.next()) {
            final String sample = text.substring(start, end).trim();
            if (filter.test(sample)) {
                if (!sample.isEmpty()) {
                    samples.add(type == TextStatistics.TextType.WORD ? sample.toLowerCase() : sample);
                }
            }
        }
        StringStatisticCalculator stringStatisticCalculator = new StringStatisticCalculator(locale);
        return stringStatisticCalculator.calculateStatistics(type, samples);
    }
}
