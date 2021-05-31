package info.kgeorgiy.ja.alyokhin.i18n.utils;

import info.kgeorgiy.ja.alyokhin.i18n.StatisticsData;
import info.kgeorgiy.ja.alyokhin.i18n.TextStatistics;

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

public class FileUtils {
    public static String readResourceFile(final Class<?> clazz, final String resourceName) throws IOException {
        final URL url = clazz.getResource("resources/" + resourceName);
        if (url == null) {
            throw new FileNotFoundException("Resource file not found");
        }
        try {
            return Files.readString(Path.of(url.toURI()));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Error while extracting uri");
        }
    }

    static String readFile(final Path filePath) throws IOException {
        return Files.readString(filePath);
    }

    static void writeFile(final String outputFileName, final String data) throws IOException, InvalidPathException {
        final Path outputFilePath = Paths.get(outputFileName);
        final Path parentDirectory = outputFilePath.getParent();
        if (parentDirectory != null) {
            Files.createDirectories(parentDirectory);
        }
        Files.writeString(Paths.get(outputFileName), data, StandardCharsets.UTF_8);
    }

    static String generateReport(final Locale inputLocale, final Locale outputLocale,
                                 final Map<TextStatistics.TextType, StatisticsData<?>> data,
                                 final String fileName)
            throws IOException {
        ResourceBundle bundle = ResourceBundle
                .getBundle("info.kgeorgiy.ja.alyokhin.i18n.resources.Bundle", outputLocale);

        final String head = readResourceFile(FileUtils.class, "head-template.html");
        final String generatedHead = String.format(head,
                bundle.getString("header"));

        final String title = readResourceFile(FileUtils.class, "title-template.html");
        final String generatedTitle = String.format(title,
                MessageFormat.format(bundle.getString("analyzedFile"), fileName));

        final String statFormat = bundle.getString("fmtStat");
        final String statFormatUnique = bundle.getString("fmtStatUnique");
        final String statFormatExample = bundle.getString("fmtStatEx");

        final MessageFormat numberFormat = new MessageFormat(bundle.getString("fmtNumber"), outputLocale);

        final Function<TextStatistics.TextType, String> sectionNameFunction =
                type -> type.toString().charAt(0) + type.toString().substring(1).toLowerCase();

        final String summary = readResourceFile(FileUtils.class, "summary-template.html");
        final List<String> summaryArgs = new ArrayList<>();
        summaryArgs.add(bundle.getString("summaryStats"));
        Arrays.stream(TextStatistics.TextType.values())
                .map(t -> MessageFormat.format(statFormat, bundle.getString("sum" + sectionNameFunction.apply(t)),
                        numberFormat.format(new Object[]{data.get(t).getTotal()})))
                .forEach(summaryArgs::add);
        final String generatedSummary = String.format(summary, (Object[]) summaryArgs.toArray(new Object[0]));

        final String section = readResourceFile(FileUtils.class, "section-template.html");
        final BiFunction<Object, MessageFormat, Object> defaultFilter =
                (o, f) -> o == null ? bundle.getString("notAvailable") : f.format(new Object[]{o});
        List<String> generatedSections = Arrays.stream(TextStatistics.TextType.values())
                .map(type -> {
                    final StatisticsData stats = data.get(type);

                    final MessageFormat dataFormat;
                    final String mean;
                    final String meanKey;
                    final String sectionName = sectionNameFunction.apply(type);
                    switch (type) {
                        case SENTENCE:
                        case LINE:
                        case WORD: {
                            dataFormat = new MessageFormat(bundle.getString("fmtString"), inputLocale);
                            mean = numberFormat.format(new Object[]{stats.getAverageLength()});
                            meanKey = "meanLen" + sectionName;
                            break;
                        }
                        default:
                            dataFormat = new MessageFormat(bundle.getString("fmt" + sectionName),
                                    type == TextStatistics.TextType.MONEY ? inputLocale : outputLocale);
                            mean = defaultFilter.apply(stats.getAverage(), dataFormat).toString();
                            meanKey = "mean" + sectionName;
                    }


                    return String.format(section,
                            bundle.getString("stats" + sectionName),
                            MessageFormat.format(statFormatUnique,
                                    bundle.getString("sum" + sectionName),
                                    stats.getTotal(),
                                    stats.getUnique(),
                                    bundle.getString("unique")),
                            MessageFormat.format(statFormat,
                                    bundle.getString("min" + sectionName),
                                    defaultFilter.apply(stats.getMin(), dataFormat)),
                            MessageFormat.format(statFormat,
                                    bundle.getString("max" + sectionName),
                                    defaultFilter.apply(stats.getMax(), dataFormat)),
                            MessageFormat.format(statFormatExample,
                                    bundle.getString("minLen" + sectionName),
                                    numberFormat.format(new Object[]{stats.getMinLength()}),
                                    defaultFilter.apply(stats.getValueWithMinLength(), dataFormat)),
                            MessageFormat.format(statFormatExample,
                                    bundle.getString("maxLen" + sectionName),
                                    numberFormat.format(new Object[]{stats.getMaxLength()}),
                                    defaultFilter.apply(stats.getValueWithMaxLength(), dataFormat)),
                            MessageFormat.format(statFormat,
                                    bundle.getString(meanKey),
                                    mean));
                }).collect(Collectors.toList());

        final String report = readResourceFile(FileUtils.class, "report-template.html");
        return String.format(report,
                generatedHead,
                generatedTitle,
                generatedSummary,
                generatedSections.get(0),
                generatedSections.get(1),
                generatedSections.get(2),
                generatedSections.get(3),
                generatedSections.get(4),
                generatedSections.get(5));
    }
}
