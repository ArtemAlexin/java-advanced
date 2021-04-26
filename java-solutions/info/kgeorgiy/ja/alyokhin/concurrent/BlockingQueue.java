package info.kgeorgiy.ja.alyokhin.concurrent;

import java.util.ArrayDeque;
import java.util.Queue;

class BlockingQueue {
    private final Queue<Task<?>> taskQueue = new ArrayDeque<>();

    public synchronized void addTask(final Task<?> task) {
        taskQueue.add(task);
        // :NOTE: notify() is just fine (and more efficient) here
        notifyAll();
    }

    public synchronized Runnable getNextRunnable() throws InterruptedException {
        while (taskQueue.isEmpty()) {
            wait();
        }
        final Task<?> element = taskQueue.element();
        final Runnable task = element.getNext();
        if (element.allStarted()) {
            taskQueue.remove();
        }
        return task;
    }

    public synchronized void stop() {
        taskQueue.forEach(Task::stop);
    }
}
