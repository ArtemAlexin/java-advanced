package info.kgeorgiy.ja.alyokhin.i18n.calculators;

import info.kgeorgiy.ja.alyokhin.i18n.StatisticsData;
import info.kgeorgiy.ja.alyokhin.i18n.TextStatistics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

public class NumberStatisticCalculator extends AbstractStatisticCalculator<Number> {
    @Override
    public StatisticsData<Number> calculateStatistics(final TextStatistics.TextType type, List<Number> values) {
        StatisticsData<Number> stats = calculateCommonStatistics(type, values, n -> n.toString().length(),
                Comparator.comparingDouble(Number::doubleValue));
        stats.setAverage(values.isEmpty() ? null : values.stream().map(n -> BigDecimal.valueOf(n.doubleValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(stats.getTotal()), RoundingMode.HALF_EVEN));
        return stats;
    }
}
