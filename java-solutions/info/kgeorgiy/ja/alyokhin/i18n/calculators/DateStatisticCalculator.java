package info.kgeorgiy.ja.alyokhin.i18n.calculators;

import info.kgeorgiy.ja.alyokhin.i18n.StatisticsData;
import info.kgeorgiy.ja.alyokhin.i18n.TextStatistics;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class DateStatisticCalculator extends AbstractStatisticCalculator<Date> {
    @Override
    public StatisticsData<Date> calculateStatistics(TextStatistics.TextType type, List<Date> values) {
        StatisticsData<Date> stats = calculateCommonStatistics(TextStatistics.TextType.DATE, values,
                n -> n.toString().length(), Comparator.comparingLong(Date::getTime));
        stats.setAverage(values.isEmpty() ? null : new Date((long)values.stream().mapToLong(Date::getTime).average().getAsDouble()));
        return stats;
    }
}
