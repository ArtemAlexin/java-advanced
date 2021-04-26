package info.kgeorgiy.ja.alyokhin.concurrent;

import java.util.*;
import java.util.function.Function;

class Task<R> {
    private final Queue<Runnable> tasks = new ArrayDeque<>();

    private final List<R> result;
    private final List<RuntimeException> exceptions = new ArrayList<>();

    private boolean stopped = false;
    private int leftToStart;
    private int leftToComplete;


    public <T> Task(final Function<? super T, ? extends R> mappingFunction,
                    final List<? extends T> list) {
        this.result = new ArrayList<>(Collections.nCopies(list.size(), null));
        this.leftToStart = list.size();
        this.leftToComplete = list.size();

        for (int i = 0; i < list.size(); i++) {
            final T value = list.get(i);
            final int index = i;
            tasks.add(() -> {
                try {
                    setResult(index, mappingFunction.apply(value));
                } catch (final RuntimeException e) {
                    addException(e);
                } finally {
                    complete();
                }
            });
        }
    }

    private int action(int value) {
        return Math.max(0, value - 1);
    }

    private void startTask() {
        leftToStart = action(leftToStart);
    }

    private void completeTask() {
        leftToComplete = action(leftToComplete);
    }

    private void throwIfErrors() {
        if (!exceptions.isEmpty()) {
            throw exceptions.stream().reduce((x, y) -> {
                x.addSuppressed(y);
                return x;
            }).get();
        }
    }

    private synchronized void setResult(final int index, final R value) {
        if (stopped) {
            return;
        }
        result.set(index, value);
    }

    private synchronized void addException(final RuntimeException e) {
        if (stopped) {
            return;
        }
        exceptions.add(e);
    }

    private synchronized void complete() {
        completeTask();
        if (leftToComplete == 0) {
            stop();
        }
    }

    public synchronized List<R> getResult() throws InterruptedException {
        while (!stopped) {
            wait();
        }
        throwIfErrors();
        return result;
    }

    public synchronized void stop() {
        this.stopped = true;
        notify();
    }

    public synchronized Runnable getNext() {
        startTask();
        return tasks.poll();
    }

    public synchronized boolean allStarted() {
        return leftToStart == 0;
    }
}
