package info.kgeorgiy.ja.alyokhin.i18n.calculators;

import info.kgeorgiy.ja.alyokhin.i18n.StatisticsData;
import info.kgeorgiy.ja.alyokhin.i18n.TextStatistics;

import java.text.Collator;
import java.util.List;
import java.util.Locale;

public class StringStatisticCalculator extends AbstractStatisticCalculator<String>{
    private Locale locale;

    public StringStatisticCalculator(Locale locale) {
        this.locale = locale;
    }

    @Override
    public StatisticsData<String> calculateStatistics(TextStatistics.TextType type, List<String> values) {
        return calculateCommonStatistics(type, values, String::length,
                (String s, String t) -> Collator.getInstance(locale).compare(s, t));
    }
}
