package info.kgeorgiy.ja.alyokhin.i18n;

import info.kgeorgiy.ja.alyokhin.i18n.calculators.DateStatisticCalculator;
import info.kgeorgiy.ja.alyokhin.i18n.calculators.NumberStatisticCalculator;
import info.kgeorgiy.ja.alyokhin.i18n.calculators.StringStatisticCalculator;
import info.kgeorgiy.ja.alyokhin.i18n.collectors.DateStatCollector;
import info.kgeorgiy.ja.alyokhin.i18n.collectors.MoneyStatCollector;
import info.kgeorgiy.ja.alyokhin.i18n.collectors.NumberStatCollector;
import info.kgeorgiy.ja.alyokhin.i18n.collectors.StringStatCollector;
import info.kgeorgiy.ja.alyokhin.i18n.parsers.DateParser;
import info.kgeorgiy.ja.alyokhin.i18n.parsers.MoneyParser;

import java.text.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class TextStatistics {
    public enum TextType {
        NUMBER,
        MONEY,
        DATE,
        SENTENCE,
        LINE,
        WORD
    }

    private static void putStringStatistics(Map<TextType, StatisticsData<?>> map,
                                            TextType type,
                                            Function<Locale, BreakIterator> breakIteratorFactory,
                                            String text,
                                            Locale inputLocale,
                                            Predicate<String> predicate) {
        map.put(type, new StringStatCollector(inputLocale, text, breakIteratorFactory.apply(inputLocale), predicate, type).collect());
    }

    public static Map<TextType, StatisticsData<?>> getStatistics(String text, Locale inputLocale) {
        final Map<TextType, StatisticsData<?>> map = new HashMap<>();

        putStringStatistics(map, TextType.SENTENCE, BreakIterator::getSentenceInstance, text, inputLocale,
                s -> true);
        putStringStatistics(map, TextType.LINE, BreakIterator::getLineInstance, text, inputLocale, s -> true);
        putStringStatistics(map, TextType.WORD, BreakIterator::getWordInstance, text, inputLocale,
                s -> !s.isEmpty() && Character.isLetter(s.charAt(0)));
        map.put(TextType.NUMBER, new NumberStatCollector(inputLocale, text).collect());
        map.put(TextType.MONEY, new MoneyStatCollector(inputLocale, text).collect());
        map.put(TextType.DATE, new DateStatCollector(inputLocale, text).collect());
        return map;
    }
}
