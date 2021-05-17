package info.kgeorgiy.ja.alyokhin.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

class CrawlerLoadImpl {
    private Downloader downloader;
    private int perHost;
    private ExecutorService loaderExecutorService;
    private ExecutorService processorExecutorService;
    private int depth;
    private Map<String, IOException> errors = new ConcurrentHashMap<>();
    private Set<String> visited = ConcurrentHashMap.newKeySet();
    private Queue<String> loaded = new LinkedBlockingQueue<>();
    private Map<String, HostTask> hostTaskMap = new ConcurrentHashMap<>();
    private String inititalLink;
    private Set<String> hosts;
    private boolean visitHosts;

    private CrawlerLoadImpl() {
    }

    public Result getResult() {
        fixedDepthBfs(inititalLink);
        return new Result(new ArrayList<>(loaded), errors);
    }

    private void fixedDepthBfs(String initialLink) {
        List<String> previousDepthLink = List.of(initialLink);
        for (int currentDepth = 1; currentDepth <= depth; currentDepth++) {
            Phaser phaser = new Phaser(1);
            boolean processNextDepth = (currentDepth < depth);
            Queue<String> currentDepthLink = new LinkedBlockingQueue<>();
            previousDepthLink.stream()
                    .filter(visited::add)
                    .forEach(x -> pass(x, currentDepthLink, processNextDepth, phaser));
            phaser.arriveAndAwaitAdvance();
            previousDepthLink = new ArrayList<>(currentDepthLink);
        }
    }

    private void pass(String link,
                      Queue<String> currentDepthLink,
                      boolean processNextDepth,
                      Phaser phaser) {
        String host;
        try {
            host = URLUtils.getHost(link);
        } catch (MalformedURLException e) {
            errors.put(link, e);
            return;
        }
        if (!this.visitHosts || hosts.contains(host)) {
            phaser.register();
            hostTaskMap.computeIfAbsent(host, x -> new HostTask()).addTask(() -> {
                try {
                    Document document = downloader.download(link);
                    loaded.add(link);
                    if (processNextDepth) {
                        extractLinks(currentDepthLink, phaser, document);
                    }
                } catch (IOException e) {
                    errors.put(link, e);
                } finally {
                    phaser.arriveAndDeregister();
                }
            });
        }
    }

    private void extractLinks(Queue<String> currentDepthLink, Phaser phaser, Document document) {
        phaser.register();
        processorExecutorService.submit(() -> {
            try {
                currentDepthLink.addAll(document.extractLinks());
            } catch (IOException ignored) {

            } finally {
                phaser.arriveAndDeregister();
            }
        });
    }

    private class HostTask {
        private final Semaphore semaphore;

        private HostTask() {
            semaphore = new Semaphore(perHost);
        }

        private void addTask(final Runnable task) {
            loaderExecutorService.submit(() -> {
                semaphore.acquireUninterruptibly();
                task.run();
                semaphore.release();
            });
        }
    }

    static class Builder {
        private CrawlerLoadImpl crawlerLoad;

        public Builder() {
            this.crawlerLoad = new CrawlerLoadImpl();
        }

        public Builder withDownloader(Downloader downloader) {
            crawlerLoad.downloader = downloader;
            return this;
        }

        public Builder withHosts(List<String> hosts) {
            crawlerLoad.hosts = new HashSet<>(hosts);
            crawlerLoad.visitHosts = true;
            return this;
        }

        public Builder withPerHost(int perHost) {
            crawlerLoad.perHost = perHost;
            return this;
        }

        public Builder withInitialLink(String link) {
            crawlerLoad.inititalLink = link;
            return this;
        }

        public Builder withLoaderExecutorService(ExecutorService loaderExecutorService) {
            crawlerLoad.loaderExecutorService = loaderExecutorService;
            return this;
        }

        public Builder withProcessorExecutorService(ExecutorService processorExecutorService) {
            crawlerLoad.processorExecutorService = processorExecutorService;
            return this;
        }

        public Builder withDepth(int depth) {
            crawlerLoad.depth = depth;
            return this;
        }

        public CrawlerLoadImpl build() {
            CrawlerLoadImpl copy = crawlerLoad;
            crawlerLoad = null;
            return copy;
        }
    }
}
