package info.kgeorgiy.ja.alyokhin.i18n.test;

import info.kgeorgiy.ja.alyokhin.i18n.FileUtils;
import info.kgeorgiy.ja.alyokhin.i18n.StatisticsData;
import info.kgeorgiy.ja.alyokhin.i18n.TextStatistics;
import org.junit.jupiter.api.DisplayName;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TextStatisticsTest {
    private Locale locale;

    public static Stream<Arguments> languages() {
        return Stream.of(
                Arguments.of("en-US"),
                Arguments.of("ru-RU")
        );
    }

    private void test(Map<TextStatistics.TextType, StatisticsData<?>> statistics,
            String testName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ResourceBundle bundle = ResourceBundle.getBundle("info.kgeorgiy.ja.alyokhin.i18n.test.resources." + testName, locale);
        Class<?> token = StatisticsData.class;
        List<String> fieldNames = Arrays.stream(token.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
        for (TextStatistics.TextType type : TextStatistics.TextType.values()) {
            final StatisticsData<?> data = statistics.get(type);
            for (String fieldName : fieldNames) {
                compare(bundle, token, type, data, fieldName);
            }
        }
    }
    private Method extractMethod(Class<?> token, String fieldName) throws NoSuchMethodException {
        return token.getMethod("get" +
                Character.toUpperCase(fieldName.charAt(0)) +
                fieldName.substring(1));
    }
    private void compare(ResourceBundle bundle, Class<?> token, TextStatistics.TextType type, StatisticsData<?> data, String fieldName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method getter = extractMethod(token, fieldName);
        Object obj1 = getter.invoke(data);
        String calculated = Objects.toString(obj1);
        try {
            String expected = bundle.getString(type.toString().toLowerCase() + "_" +
                    fieldName);
            assertEquals(expected, calculated);
        } catch (MissingResourceException e) {
            // Ignored.
        }
    }

    private void commonActions(String testName, String languageTag) throws IOException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        this.locale = new Locale.Builder().setLanguageTag(languageTag).build();
        final String text = FileUtils.readResource(TextStatisticsTest.class, testName + "_" +
                locale.getLanguage() + "_" + locale.getCountry() + ".txt");
        final Map<TextStatistics.TextType, StatisticsData<?>> statistics
                = TextStatistics.collectStatToMap(text, locale);
        test(statistics, testName);
    }

    @DisplayName("wiki")
    @ParameterizedTest
    @MethodSource("languages")
    public void wikiTest(String arg) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        commonActions("wiki", arg);
    }

    @DisplayName("num")
    @ParameterizedTest
    @MethodSource("languages")
    public void numTest(String arg) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        commonActions("num", arg);
    }

    @DisplayName("dates")
    @ParameterizedTest
    @MethodSource("languages")
    public void datesTest(String arg) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        commonActions("dates", arg);
    }

    @DisplayName("sherlock")
    @ParameterizedTest
    @MethodSource("languages")
    public void sherlockTest(String arg) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        commonActions("sherlock", arg);
    }
}
