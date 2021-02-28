package info.kgeorgiy.ja.alyokhin.arrayset;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;


public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    private final List<T> arr;
    private final Comparator<? super T> comparator;

    private ArraySet(List<T> arr, Comparator<? super T> comparator) {
        this.arr = arr;
        this.comparator = comparator;
    }

    public ArraySet(Collection<? extends T> collection, Comparator<? super T> cmp) {
        Set<T> tmp = new TreeSet<>(cmp);
        tmp.addAll(collection);
        arr = new ListWithOrder<>(new ArrayList<>(tmp));
        comparator = cmp;
    }

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Comparator<? super T> comparator) {
        this(Collections.emptyList(), comparator);
    }

    public ArraySet(Collection<? extends T> collection) {
        this(collection, null);
    }

    private Function<Integer, Integer> getResultFunction(boolean strict, boolean upperBound) {
        return x -> {
            if (x < 0) {
                return -(x + 1) - (upperBound ? 0 : 1);
            }
            return (strict ? x + (upperBound ? 1 : -1) : x);
        };
    }

    private int findElementAndApply(T t, Function<Integer, Integer> resultFunction) {
        int idx = Collections.binarySearch(arr, t, comparator);
        return resultFunction.apply(idx);
    }

    @Override
    public T lower(T t) {
        return findBoundElement(t, true, this::findLowerBoundElementIndex);
    }

    @Override
    public T floor(T t) {
        return findBoundElement(t, false, this::findLowerBoundElementIndex);
    }

    @Override
    public T ceiling(T t) {
        return findBoundElement(t, false, this::findUpperBoundElementIndex);
    }

    @Override
    public T higher(T t) {
        return findBoundElement(t, true, this::findUpperBoundElementIndex);
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(arr).iterator();
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(new ListWithOrder<>(arr),
                Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    private NavigableSet<T> interval(int left, int right) {
        if (right < left) {
            return new ArraySet<>(comparator);
        }
        return new ArraySet<>(arr.subList(left, right), comparator);
    }

    private int findUpperBoundElementIndex(T elem, boolean strict) {
        return findElementAndApply(elem, getResultFunction(strict, true));
    }

    private int findLowerBoundElementIndex(T elem, boolean strict) {
        return findElementAndApply(elem, getResultFunction(strict, false));
    }

    private T findBoundElement(T elem, boolean strict, BiFunction<T, Boolean, Integer> indexBoundFunction) {
        int idx = indexBoundFunction.apply(elem, strict);
        return (idx < 0 || idx >= size()) ? null : arr.get(idx);
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        validateBounds(fromElement, toElement);
        int idx1 = findUpperBoundElementIndex(fromElement, !fromInclusive);
        int idx2 = findUpperBoundElementIndex(toElement, toInclusive);
        return interval(idx1, idx2);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        return interval(0, findUpperBoundElementIndex(toElement, inclusive));
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        return interval(findUpperBoundElementIndex(fromElement, !inclusive), size());
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public T first() {
        return getElement(0);
    }

    @Override
    public T last() {
        return getElement(size() - 1);
    }

    private T getElement(int i) {
        if (arr.isEmpty()) {
            throw new NoSuchElementException();
        }
        return arr.get(i);
    }

    @Override
    public int size() {
        return arr.size();
    }

    @SuppressWarnings("unchecked")
    private void validateBounds(T from, T to) {
        if (comparator == null && ((Comparable<T>) from).compareTo(to) > 0
                || comparator != null && comparator.compare(from, to) > 0) {
            throw new IllegalArgumentException();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return findElementAndApply((T) o, x -> x) >= 0;
    }
}
