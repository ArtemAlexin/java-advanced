package info.kgeorgiy.ja.alyokhin.crawler;

import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WebCrawler implements Crawler {
    public static final int TIMEOUT = 800;
    private final Downloader downloader;
    private final int perHost;
    private final ExecutorService loaderExecutorService;
    private final ExecutorService processorExecutorService;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        loaderExecutorService = Executors.newFixedThreadPool(downloaders);
        processorExecutorService = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
    }

    @Override
    public Result download(String url, int depth) {
        return new CrawlerLoadImpl(downloader,
                perHost,
                url,
                loaderExecutorService,
                processorExecutorService,
                depth).getResult();
    }

    @Override
    public void close() {
        shutdownAndAwaitTermination(processorExecutorService);
        shutdownAndAwaitTermination(loaderExecutorService);
    }

    private void shutdownAndAwaitTermination(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
