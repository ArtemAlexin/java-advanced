package info.kgeorgiy.ja.alyokhin.i18n;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static info.kgeorgiy.ja.alyokhin.i18n.FileUtils.PROPS.*;

public class FileUtils {
    enum PROPS {
        AVERAGE("average"),
        HEADER("name"),
        AVERAGE_LEN("averageLen"),
        TITLE("analyzedFile"),
        FORMAT_STAT("formatStat"),
        FORMAT_UNIQUE("formatStatUnique"),
        FORMAT_EX("formatStatEx"),
        FORMAT_NUM("formatNumber"),
        FORMAT_STRING("formatString"),
        FORMAT("format"),
        TOTAL_STAT("totalStats"),
        TOTAL("total"),
        UNAVAILABLE("notAvailable"),
        UNIQUE("unique"),
        MIN("min"),
        MAX("max"),
        MIN_LEN("minLen"),
        MAX_LEN("maxLen"),
        STATISTICS("stats");

        private final String prop;

        public String getName() {
            return prop;
        }

        PROPS(String prop) {
            this.prop = prop;
        }
    }

    private static final String HEADER_FILE_NAME = "head.html";
    private static final String MESSAGE_FILE_NAME = "message.html";
    private static final String TITLE_FILE_NAME = "name.html";
    private static final String SECTION_FILE_NAME = "section.html";
    private static final String TOTAL_FILE_NAME = "total.html";

    public static String readResource(Class<?> clazz, String resourceName) throws IOException {
        final URL url = clazz.getResource("resources/" + resourceName);
        if (url == null) {
            throw new FileNotFoundException("Resource file does not exist");
        }
        try {
            return read(Path.of(url.toURI()));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Uri extracting error");
        }
    }

    public static String read(Path filePath) throws IOException {
        return Files.readString(filePath);
    }

    public static void createDirectory(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    public static void writeFile(String file, String data) throws IOException, InvalidPathException {
        Path filePath = Paths.get(file);
        createDirectory(filePath);
        Files.writeString(filePath, data, StandardCharsets.UTF_8);
    }

    private static List<String> getSections(Locale inputLocale,
                                            Locale outputLocale,
                                            Map<TextStatistics.TextType, StatisticsData<?>> data,
                                            ResourceBundle bundle,
                                            String statFormat,
                                            String statFormatUnique,
                                            String statFormatExample,
                                            MessageFormat numberFormat,
                                            Function<TextStatistics.TextType, String>
                                                    sectionNameFunction) throws IOException {
        BiFunction<Object, MessageFormat, Object> filter =
                (obj, formatter) -> obj == null ?
                        bundle.getString(UNAVAILABLE.getName()) :
                        formatter.format(new Object[]{obj});
        String section = readResource(FileUtils.class, SECTION_FILE_NAME);
        return Arrays.stream(TextStatistics.TextType.values())
                .map(type -> {
                    StatisticsData<?> stats = data.get(type);
                    MessageFormat dataFormat;
                    String average;
                    String key;
                    String sectionName = sectionNameFunction.apply(type);
                    switch (type) {
                        case SENTENCE, LINE, WORD -> {
                            dataFormat = new MessageFormat(bundle.getString(FORMAT_STRING.getName()), inputLocale);
                            average = numberFormat.format(new Object[]{stats.getAverageLength()});
                            key = AVERAGE_LEN.getName() + sectionName;
                        }
                        default -> {
                            dataFormat = new MessageFormat(bundle.getString(FORMAT.getName() + sectionName),
                                    type == TextStatistics.TextType.MONEY ? inputLocale : outputLocale);
                            average = filter.apply(stats.getAverage(), dataFormat).toString();
                            key = AVERAGE.getName() + sectionName;
                        }
                    }
                    return String.format(section,
                            bundle.getString(STATISTICS.getName() + sectionName),
                            getFormatUniq(bundle, statFormatUnique, stats, sectionName),
                            getFormatStat(bundle, statFormat, sectionName, filter.apply(stats.getMin(), dataFormat)),
                            getFormatStat(bundle, statFormat, sectionName, filter.apply(stats.getMax(), dataFormat)),
                            getFormatEx(bundle, statFormatExample, numberFormat, sectionName, stats.getMinLength(), filter.apply(stats.getValueWithMinLength(), dataFormat)),
                            getFormatEx(bundle, statFormatExample, numberFormat, sectionName, stats.getMaxLength(), filter.apply(stats.getValueWithMaxLength(), dataFormat)),
                            getFormat(bundle, statFormat, average, key));
                }).collect(Collectors.toList());
    }

    private static String getFormat(ResourceBundle bundle, String statFormat, String average, String key) {
        return MessageFormat.format(statFormat,
                bundle.getString(key),
                average);
    }

    private static String getFormatEx(ResourceBundle bundle,
                                      String statFormatExample,
                                      MessageFormat numberFormat,
                                      String sectionName,
                                      int minLength, Object apply) {
        return MessageFormat.format(statFormatExample,
                bundle.getString(MIN_LEN.getName() + sectionName),
                numberFormat.format(new Object[]{minLength}),
                apply);
    }

    private static String getFormatStat(ResourceBundle bundle,
                                        String statFormat,
                                        String sectionName,
                                        Object apply) {
        return getFormat(bundle, statFormat, (String) apply, MIN.getName() + sectionName);
    }

    private static String getFormatUniq(ResourceBundle bundle,
                                        String statFormatUnique, StatisticsData<?> stats, String sectionName) {
        return MessageFormat.format(statFormatUnique,
                bundle.getString(TOTAL.getName() + sectionName),
                stats.getTotal(),
                stats.getUnique(),
                bundle.getString(UNIQUE.getName()));
    }

    private static String getTotal(List<String> summaryArgs) throws IOException {
        String total = readResource(FileUtils.class, TOTAL_FILE_NAME);
        return String.format(total, summaryArgs.toArray(new Object[0]));
    }

    private static String getTitle(String name, ResourceBundle bundle) throws IOException {
        String title = readResource(FileUtils.class, TITLE_FILE_NAME);
        return String.format(title,
                MessageFormat.format(bundle.getString(TITLE.getName()), name));
    }

    private static String getHead(ResourceBundle bundle) throws IOException {
        String head = readResource(FileUtils.class, HEADER_FILE_NAME);
        return String.format(head,
                bundle.getString(HEADER.getName()));
    }

    public static String generate(Locale iLocale,
                                  Locale oLocale,
                                  Map<TextStatistics.TextType, StatisticsData<?>> map,
                                  String name)
            throws IOException {
        ResourceBundle bundle = ResourceBundle
                .getBundle("info.kgeorgiy.ja.alyokhin.i18n.resources.Bundle", oLocale);

        String statFormat = bundle.getString(FORMAT_STAT.getName());
        String statFormatUnique = bundle.getString(FORMAT_UNIQUE.getName());
        String statFormatExample = bundle.getString(FORMAT_EX.getName());

        String messageFormat = readResource(FileUtils.class, MESSAGE_FILE_NAME);
        MessageFormat numberFormat = new MessageFormat(bundle.getString(FORMAT_NUM.getName()), oLocale);

        String head = getHead(bundle);
        String title = getTitle(name, bundle);
        Function<TextStatistics.TextType, String> sectionNameFunction =
                type -> type.toString().charAt(0) + type.toString().substring(1).toLowerCase();
        List<String> args = new ArrayList<>();
        args.add(bundle.getString(TOTAL_STAT.getName()));
        Arrays.stream(TextStatistics.TextType.values())
                .map(t -> getFormat(bundle, statFormat,
                        numberFormat.format(new Object[]{map.get(t).getTotal()}),
                        TOTAL.getName() + sectionNameFunction.apply(t)))
                .forEach(args::add);
        String total = getTotal(args);
        List<String> sections = getSections(iLocale,
                oLocale,
                map,
                bundle,
                statFormat,
                statFormatUnique,
                statFormatExample,
                numberFormat,
                sectionNameFunction);
        String[] format = new String[3 + sections.size()];
        String[] firstElements = new String[]{head, title, total};
        System.arraycopy(firstElements, 0, format, 0, 3);
        System.arraycopy(sections.toArray(), 0, format, 3, sections.size());
        return String.format(messageFormat, (Object[]) format);
    }
}
