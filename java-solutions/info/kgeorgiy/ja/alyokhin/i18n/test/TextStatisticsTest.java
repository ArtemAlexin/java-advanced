package info.kgeorgiy.ja.alyokhin.i18n.test;

import info.kgeorgiy.ja.alyokhin.i18n.utils.FileUtils;
import info.kgeorgiy.ja.alyokhin.i18n.StatisticsData;
import info.kgeorgiy.ja.alyokhin.i18n.TextStatistics;
import org.junit.jupiter.api.DisplayName;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TextStatisticsTest {
    private Locale locale;

    public static Stream<Arguments> languages() {
        return Stream.of(
                Arguments.of("en-US")
        );

    }

    private void validateStatistics(
            final Map<TextStatistics.TextType, StatisticsData<?>> statistics,
            final String testName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        ResourceBundle bundle = ResourceBundle
                .getBundle("info.kgeorgiy.ja.alyokhin.i18n.test.resources." + testName, locale);
        final Class<?> clazz = StatisticsData.class;
        final List<String> fieldNames = Arrays.stream(clazz.getDeclaredFields()).map(Field::getName)
                .collect(Collectors.toList());
        for (TextStatistics.TextType type : TextStatistics.TextType.values()) {
            final StatisticsData<?> data = statistics.get(type);
            for (String fieldName : fieldNames) {
                try {
                    Method fieldGetter = clazz.getMethod("get" + Character.toUpperCase(fieldName.charAt(0))
                            + fieldName.substring(1));
                    final Object gotObject = fieldGetter.invoke(data);
                    final String gotString = (gotObject == null) ? "null" : gotObject.toString();

                    try {
                        final String expectedString = bundle.getString(type.toString().toLowerCase() + "_" +
                                fieldName);

                        assertEquals(expectedString, gotString);
                    } catch (final MissingResourceException e) {
                        // Ignored.
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw e;
                }
            }
        }
    }

    private void testRoutine(final String testName, String languageTag) throws IOException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        this.locale = new Locale.Builder().setLanguageTag(languageTag).build();
        final String text = FileUtils.readResourceFile(TextStatisticsTest.class, testName + "_" +
                locale.getLanguage() + "_" + locale.getCountry() + ".txt");
        final Map<TextStatistics.TextType, StatisticsData<?>> statistics
                = TextStatistics.getStatistics(text, locale);
        validateStatistics(statistics, testName);
    }

    @DisplayName("wiki")
    @ParameterizedTest
    @MethodSource("languages")
    public void wikiTest(String arg) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        testRoutine("wiki", arg);
    }

    @DisplayName("num")
    @ParameterizedTest
    @MethodSource("languages")
    public void numTest(String arg) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        testRoutine("num", arg);
    }

    @DisplayName("bigText")
    @ParameterizedTest
    @MethodSource("languages")
    public void bigTest(String arg) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        testRoutine("bigText", arg);
    }
}
