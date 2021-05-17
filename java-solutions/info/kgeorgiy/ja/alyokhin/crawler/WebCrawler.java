package info.kgeorgiy.ja.alyokhin.crawler;

import info.kgeorgiy.java.advanced.crawler.AdvancedCrawler;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WebCrawler implements AdvancedCrawler {
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

    private CrawlerLoadImpl.Builder initBuild(String url, int depth) {
        return new CrawlerLoadImpl.Builder()
                .withDownloader(downloader)
                .withPerHost(perHost)
                .withInitialLink(url)
                .withLoaderExecutorService(loaderExecutorService)
                .withProcessorExecutorService(processorExecutorService)
                .withDepth(depth);
    }

    @Override
    public Result download(String url, int depth) {
        return initBuild(url, depth).build().getResult();
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

    @Override
    public Result download(String url, int depth, List<String> hosts) {
        return initBuild(url, depth).withHosts(hosts).build().getResult();
    }
}
