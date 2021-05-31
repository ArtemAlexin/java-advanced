package info.kgeorgiy.ja.alyokhin.i18n.collectors;

import info.kgeorgiy.ja.alyokhin.i18n.StatisticsData;
import info.kgeorgiy.ja.alyokhin.i18n.TextStatistics;
import info.kgeorgiy.ja.alyokhin.i18n.calculators.NumberStatisticCalculator;
import info.kgeorgiy.ja.alyokhin.i18n.parsers.MoneyParser;

import java.util.List;
import java.util.Locale;
public class MoneyStatCollector extends AbstractStatCollectors<Number> {
    public MoneyStatCollector(Locale locale, String text) {
        super(locale, text);
    }

    @Override
    public StatisticsData<Number> collect() {
        MoneyParser moneyParser = new MoneyParser(locale);
        List<Number> samples = commonCollect(text, locale, moneyParser::parse);
        NumberStatisticCalculator numberStatisticCalculator = new NumberStatisticCalculator();
        return numberStatisticCalculator.calculateStatistics(TextStatistics.TextType.MONEY, samples);
    }
}
