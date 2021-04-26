package info.kgeorgiy.ja.alyokhin.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link ParallelMapper}.
 */
public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> threadList;
    private final BlockingQueue taskQueue;

    private boolean closed = false;

    private static void joinForce(Thread thread) {
        while (true) {
            try {
                thread.join();
                break;
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void joinAll() {
        threadList.forEach(ParallelMapperImpl::joinForce);
    }

    private Runnable createJob() {
        return () -> {
            try {
                while (!Thread.interrupted()) {
                    taskQueue.getNextRunnable().run();
                }
            } catch (final InterruptedException ignored) {

            } finally {
                Thread.currentThread().interrupt();
            }
        };
    }

    private List<Thread> createThreads(int size, Runnable task) {
        return Stream.generate(() -> new Thread(task))
                .limit(size)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Basic constructor.
     *
     * @param threads Number of threads
     */
    public ParallelMapperImpl(final int threads) {
        if (threads <= 0) {
            throw new IllegalArgumentException("Invalid number of threads.");
        }
        taskQueue = new BlockingQueue();
        this.threadList = createThreads(threads, createJob());
        this.threadList.forEach(Thread::start);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, R> List<R> map(final Function<? super T, ? extends R> function, final List<? extends T> list)
            throws InterruptedException {
        final Task<R> task;

        synchronized (this) {
            if (closed) {
                throw new IllegalStateException("Adding tasks to the closed mapper is forbidden");
            }
            task = new Task<>(function, list);
            taskQueue.addTask(task);
        }

        return task.getResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        synchronized (this) {
            closed = true;
            threadList.forEach(Thread::interrupt);
            taskQueue.forEach(Task::stop);
        }
        joinAll();
    }
}

