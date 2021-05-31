package info.kgeorgiy.ja.alyokhin.i18n.collectors;

import info.kgeorgiy.ja.alyokhin.i18n.StatisticsData;
import info.kgeorgiy.ja.alyokhin.i18n.TextStatistics;
import info.kgeorgiy.ja.alyokhin.i18n.calculators.DateStatisticCalculator;
import info.kgeorgiy.ja.alyokhin.i18n.parsers.DateParser;

import java.util.Date;
import java.util.List;
import java.util.Locale;


public class DateStatCollector extends AbstractStatCollectors<Date> {
    public DateStatCollector(Locale locale, String text) {
        super(locale, text);
    }

    @Override
    public StatisticsData<Date> collect() {
        DateParser dateParser = new DateParser(locale);
        List<Date> samples = commonCollect(text, locale, dateParser::parse);
        DateStatisticCalculator dateStatisticCalculator = new DateStatisticCalculator();
        return dateStatisticCalculator.calculateStatistics(TextStatistics.TextType.DATE, samples);
    }
}
