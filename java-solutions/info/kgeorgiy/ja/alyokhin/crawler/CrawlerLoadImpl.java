package info.kgeorgiy.ja.alyokhin.crawler;

import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;
import info.kgeorgiy.java.advanced.crawler.URLUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

class CrawlerLoadImpl {
    private final Downloader downloader;
    private final int perHost;
    private final ExecutorService loaderExecutorService;
    private final ExecutorService processorExecutorService;
    private final int depth;
    private final Map<String, IOException> errors = new ConcurrentHashMap<>();
    private final Set<String> visited = ConcurrentHashMap.newKeySet();
    private final Queue<String> loaded = new LinkedBlockingQueue<>();
    private final Map<String, HostTask> hostTaskMap = new ConcurrentHashMap<>();
    private final String inititalLink;
    private final Set<String> hosts;
    private final boolean visitHosts;
    CrawlerLoadImpl(Downloader downloader,
                    List<String> hosts,
                    int perHost,
                    String initialLink,
                    ExecutorService loaderExecutorService,
                    ExecutorService processorExecutorService,
                    boolean visitHosts,
                    int depth) {
        this.downloader = downloader;
        this.perHost = perHost;
        this.loaderExecutorService = loaderExecutorService;
        this.processorExecutorService = processorExecutorService;
        this.depth = depth;
        this.visitHosts = visitHosts;
        this.inititalLink = initialLink;
        this.hosts = new HashSet<>(hosts);
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
}
