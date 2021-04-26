package info.kgeorgiy.ja.alyokhin.concurrent;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

class BlockingQueue {
    private final Queue<Task<?>> taskQueue = new ArrayDeque<>();

    public synchronized void addTask(final Task<?> task) {
        taskQueue.add(task);
        notifyAll();
    }

    public synchronized Runnable getNextRunnable() throws InterruptedException {
        while (taskQueue.isEmpty()) {
            wait();
        }

        final Task<?> head = taskQueue.element();
        final Runnable task = head.getNext();
        if (head.allStarted()) {
            taskQueue.remove();
        }
        return task;
    }

    public synchronized void forEach(final Consumer<Task<?>> consumer) {
        taskQueue.forEach(consumer);
    }
}
