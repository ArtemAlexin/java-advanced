package info.kgeorgiy.ja.alyokhin.i18n.collectors;

import info.kgeorgiy.ja.alyokhin.i18n.StatisticsData;
import info.kgeorgiy.ja.alyokhin.i18n.TextStatistics;
import info.kgeorgiy.ja.alyokhin.i18n.calculators.NumberStatisticCalculator;
import info.kgeorgiy.ja.alyokhin.i18n.parsers.DateParser;
import info.kgeorgiy.ja.alyokhin.i18n.parsers.MoneyParser;

import java.text.BreakIterator;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NumberStatCollector extends AbstractStatCollector<Number> {

    public NumberStatCollector(Locale locale, String text) {
        super(locale, text);
    }

    @Override
    public StatisticsData<Number> collect() {
        final BreakIterator breakIterator = BreakIterator.getWordInstance(locale);
        breakIterator.setText(text);
        final List<Number> samples = new ArrayList<>();
        final NumberFormat format = NumberFormat.getNumberInstance(locale);
        DateParser dateParser = new DateParser(locale);
        MoneyParser moneyParser = new MoneyParser(locale);
        for (int start = breakIterator.first(), end = breakIterator.next(), ignoreLimit = 0;
             end != BreakIterator.DONE;
             start = end, end = breakIterator.next()) {
            if (start < ignoreLimit) {
                continue;
            }
            final ParsePosition position = new ParsePosition(start);
            if (dateParser.parse(text, position) != null || moneyParser.parse(text, position) != null) {
                ignoreLimit = position.getIndex();
                continue;
            }
            final Number sample = format.parse(text, new ParsePosition(start));
            if (sample != null) {
                samples.add(sample);
            }
        }
        NumberStatisticCalculator numberStatisticCalculator = new NumberStatisticCalculator();
        return numberStatisticCalculator.calculateStatistics(TextStatistics.TextType.NUMBER, samples);
    }
}
