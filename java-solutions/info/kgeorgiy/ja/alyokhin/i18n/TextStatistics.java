package info.kgeorgiy.ja.alyokhin.i18n;

import info.kgeorgiy.ja.alyokhin.i18n.collectors.*;
import info.kgeorgiy.ja.alyokhin.i18n.utils.ConsoleLogger;
import info.kgeorgiy.ja.alyokhin.i18n.utils.Logger;
import info.kgeorgiy.ja.alyokhin.i18n.utils.ValidationException;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
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

    private static final Logger logger = ConsoleLogger.getInstance();

    private static void putStringStatistics(Map<TextType, StatisticsData<?>> map,
                                            TextType type,
                                            Function<Locale, BreakIterator> breakIteratorFactory,
                                            String text,
                                            Locale inputLocale,
                                            Predicate<String> predicate) {
        map.put(type, new StringStatCollector(inputLocale, text, breakIteratorFactory.apply(inputLocale), predicate, type).collect());
    }

    private static void putStatistics(Map<TextType, StatisticsData<?>> map,
                                      TextType type, AbstractStatCollector<?> collector) {
        map.put(type, collector.collect());
    }

    public static Map<TextType, StatisticsData<?>> collectStatToMap(String text, Locale inputLocale) {
        final Map<TextType, StatisticsData<?>> map = new HashMap<>();
        putStringStatistics(map, TextType.SENTENCE, BreakIterator::getSentenceInstance, text, inputLocale,
                s -> true);
        putStringStatistics(map, TextType.LINE, BreakIterator::getLineInstance, text, inputLocale, s -> true);
        putStringStatistics(map, TextType.WORD, BreakIterator::getWordInstance, text, inputLocale,
                s -> !s.isEmpty() && Character.isLetter(s.charAt(0)));
        putStatistics(map, TextType.NUMBER, new NumberStatCollector(inputLocale, text));
        putStatistics(map, TextType.MONEY, new MoneyStatCollector(inputLocale, text));
        putStatistics(map, TextType.DATE, new DateStatCollector(inputLocale, text));
        return map;
    }

    private static void validateArgs(String[] args) throws ValidationException {
        if (args.length != 4 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            throw new ValidationException("correct usage - TextStatistics inputLocale outputLocale inputFileName outputFileName");
        }
    }

    private static void validateLocale(Locale locale) throws ValidationException {
        if (!(locale.getLanguage().equals("ru") || locale.getLanguage().equals("en"))) {
            throw new ValidationException("Unsupported locale provided");
        }
    }

    private static String getTextAndValidate(String path) throws ValidationException {
        try {
            return FileUtils.read(Path.of(path));
        } catch (final IOException | InvalidPathException e) {
            throw new ValidationException("Failed to read input file " + e.getMessage());
        }
    }

    private static String getMessageAndValidate(Locale iLocale, Locale oLocale, Map<TextStatistics.TextType, StatisticsData<?>> statistics, String path) throws ValidationException {
        try {
            return FileUtils.generate(iLocale, oLocale, statistics, path);
        } catch (final IOException e) {
            throw new ValidationException("Failed to generate report " + e.getMessage());
        }
    }

    private static void writeAndValidate(String name, String message) throws ValidationException {
        try {
            FileUtils.writeFile(name, message);
        } catch (final IOException | InvalidPathException e) {
            throw new ValidationException("Failed to write a report file " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            validateArgs(args);
            final Locale inputLocale = new Locale.Builder().setLanguageTag(args[0]).build();
            final Locale outputLocale = new Locale.Builder().setLanguageTag(args[1]).build();
            validateLocale(outputLocale);
            final String text = getTextAndValidate(args[2]);
            Map<TextStatistics.TextType, StatisticsData<?>> statistics =
                    TextStatistics.collectStatToMap(text, inputLocale);
            final String message = getMessageAndValidate(inputLocale, outputLocale, statistics, args[2]);
            writeAndValidate(args[3], message);
        } catch (ValidationException e) {
            logger.logError("Error:", e);
        }
    }
}
