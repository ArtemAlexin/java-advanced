package info.kgeorgiy.ja.alyokhin.i18n;

import info.kgeorgiy.ja.alyokhin.i18n.collectors.DateStatCollector;
import info.kgeorgiy.ja.alyokhin.i18n.collectors.MoneyStatCollector;
import info.kgeorgiy.ja.alyokhin.i18n.collectors.NumberStatCollector;
import info.kgeorgiy.ja.alyokhin.i18n.collectors.StringStatCollector;

import java.text.BreakIterator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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

    private static void putStringStatistics(final Map<TextType, StatisticsData<?>> map,
                                            final TextType type,
                                            final Function<Locale, BreakIterator> breakIteratorFactory,
                                            final String text,
                                            final Locale inputLocale,
                                            final Predicate<String> predicate) {
        map.put(type, new StringStatCollector(inputLocale, text, breakIteratorFactory.apply(inputLocale), predicate, type).collect());
    }

    public static Map<TextType, StatisticsData<?>> getStatistics(final String text, final Locale inputLocale) {
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

