package info.kgeorgiy.ja.alyokhin.concurrent;

import info.kgeorgiy.java.advanced.concurrent.AdvancedIP;
import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements AdvancedIP {
    private static <T> List<List<T>> parallelizeList(int threads, List<T> list) {
        int realNumberOfThreads = Math.min(threads, list.size());
        int sizeOfBucket = list.size() / realNumberOfThreads;
        int sizeRem = list.size() % realNumberOfThreads;
        List<List<T>> listOfTasks = new ArrayList<>();
        int left = 0;
        for (int i = 0; i < realNumberOfThreads; i++) {
            int toAdd = (sizeRem-- > 0 ? 1 : 0);
            int right = left + sizeOfBucket + toAdd;
            listOfTasks.add(list.subList(left, right));
            left = right;
        }
        return listOfTasks;
    }

    private static <T, R> List<R> runAllThreads(int threads, List<T> list,
                                                Function<Stream<T>, R> function) throws InterruptedException {
        List<List<T>> listOfTasks = parallelizeList(threads, list);
        List<Thread> threadList = new ArrayList<>();
        List<R> res = new ArrayList<>(Collections.nCopies(listOfTasks.size(), null));
        for (int i = 0; i < listOfTasks.size(); i++) {
            final int idx = i;
            Thread thread = new Thread(() -> res.set(idx,
                    function.apply(listOfTasks.get(idx).stream())));
            threadList.add(thread);
            thread.start();
        }
        stopAllThreads(threadList);
        return res;
    }

    private static void stopAllThreads(List<Thread> threadList) throws InterruptedException {
        InterruptedException interruptedException = null;
        for (int i = 0; i < threadList.size(); i++) {
            try {
                threadList.get(i).join();
            } catch (InterruptedException e) {
                threadList.get(i).interrupt();
                if (Objects.isNull(interruptedException)) {
                    interruptedException = new InterruptedException("One off threads was interrupted");
                }
                interruptedException.addSuppressed(e);
                i--;
            }
        }
        if (Objects.nonNull(interruptedException)) {
            throw interruptedException;
        }
    }

    private static <T, M, R> R apply(int threads, List<T> list,
                                     Function<Stream<T>, M> function,
                                     Function<Stream<M>, R> resultFunction) throws InterruptedException {
        List<M> result = runAllThreads(threads, list, function);
        return resultFunction.apply(result.stream());
    }

    private static <T> T apply(int threads, List<T> list,
                               Function<Stream<T>, T> function) throws InterruptedException {
        return apply(threads, list, function, function);
    }

    private static <T, R> List<R> getListResult(int threads,
                                                List<T> list,
                                                Function<Stream<T>, Stream<? extends R>> function) throws InterruptedException {
        return apply(threads, list,
                function,
                x -> x.flatMap(Function.<Stream<? extends R>>identity()).collect(Collectors.toList()));
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return apply(threads, values,
                x -> x.map(Object::toString).collect(Collectors.joining()),
                x -> x.collect(Collectors.joining()));
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return getListResult(threads, values, x -> x.filter(predicate));
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return getListResult(threads, values, x -> x.map(f));
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return apply(threads, values,
                x -> x.max(comparator).orElseThrow());
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !any(threads, values, predicate.negate());
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return apply(threads, values,
                x -> x.anyMatch(predicate),
                x -> x.anyMatch(Boolean::booleanValue));
    }

    @Override
    public <T> T reduce(int threads, List<T> values, Monoid<T> monoid) throws InterruptedException {
        return mapReduce(threads, values, Function.identity(), monoid);
    }

    @Override
    public <T, R> R mapReduce(int threads, List<T> values, Function<T, R> lift, Monoid<R> monoid) throws InterruptedException {
        return apply(threads, values,
                x -> x.map(lift).reduce(monoid.getIdentity(), monoid.getOperator()),
                x -> x.reduce(monoid.getIdentity(), monoid.getOperator()));
    }
}
