package info.kgeorgiy.ja.alyokhin.i18n;

public class StatisticsData<T> {
    private TextStatistics.TextType type;
    private int total;
    private int unique;
    private T min;
    private T max;
    private T average;
    private int minLength;
    private T valueWithMinLength;
    private int maxLength;
    private T valueWithMaxLength;
    private double averageLength;

    public StatisticsData() {
    }

    public TextStatistics.TextType getType() {
        return type;
    }

    public int getTotal() {
        return total;
    }

    public void setType(TextStatistics.TextType type) {
        this.type = type;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setUnique(int unique) {
        this.unique = unique;
    }

    public void setMin(T min) {
        this.min = min;
    }

    public void setMax(T max) {
        this.max = max;
    }

    public void setAverage(T average) {
        this.average = average;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public void setValueWithMinLength(T valueWithMinLength) {
        this.valueWithMinLength = valueWithMinLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public void setValueWithMaxLength(T valueWithMaxLength) {
        this.valueWithMaxLength = valueWithMaxLength;
    }

    public void setAverageLength(double averageLength) {
        this.averageLength = averageLength;
    }

    public int getUnique() {
        return unique;
    }

    public T getMin() {
        return min;
    }

    public T getMax() {
        return max;
    }

    public T getAverage() {
        return average;
    }

    public int getMinLength() {
        return minLength;
    }

    public T getValueWithMinLength() {
        return valueWithMinLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public T getValueWithMaxLength() {
        return valueWithMaxLength;
    }

    public double getAverageLength() {
        return averageLength;
    }
}
