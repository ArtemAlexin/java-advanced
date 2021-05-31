package info.kgeorgiy.ja.alyokhin.i18n.calculators;

import info.kgeorgiy.ja.alyokhin.i18n.StatisticsData;
import info.kgeorgiy.ja.alyokhin.i18n.TextStatistics;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractStatisticCalculator<T> {
    protected StatisticsData<T> calculateCommonStatistics(final TextStatistics.TextType type,
                                                          final List<T> samples,
                                                          final Function<T, Integer> sampleLength,
                                                          final Comparator<T> valueComparator) {
        final StatisticsData<T> stats = new StatisticsData<>();
        stats.setType(type);
        if (samples.isEmpty()) {
            return stats;
        }
        stats.setTotal(samples.size());
        samples.sort(Comparator.comparing(sampleLength));
        stats.setMinLength(sampleLength.apply(samples.get(0)));
        stats.setValueWithMinLength(samples.get(0));
        stats.setMaxLength(sampleLength.apply(samples.get(samples.size() - 1)));
        stats.setValueWithMaxLength(samples.get(samples.size() - 1));
        stats.setAverageLength((double) samples.stream().map(sampleLength).reduce(0, Integer::sum) /
                samples.size());
        samples.sort(valueComparator);
        stats.setMin(samples.get(0));
        stats.setMax(samples.get(samples.size() - 1));
        int countUnique = 1;
        for (int i = 0; i < samples.size() - 1; i++) {
            if (valueComparator.compare(samples.get(i), samples.get(i + 1)) != 0) {
               countUnique++;
            }
        }
        stats.setUnique(countUnique);
        return stats;
    }
    public abstract StatisticsData<T> calculateStatistics(final TextStatistics.TextType type, List<T> values);
}
