package info.kgeorgiy.ja.alyokhin.arrayset;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;

public class ListWithOrder<T> extends AbstractList<T> implements RandomAccess {
    private final List<T> arr;

    public ListWithOrder(List<T> arr) {
        this.arr = arr;
    }

    public int getIndex(int index) {
        return index;
    }

    @Override
    public T get(int index) {
        return arr.get(getIndex(index));
    }

    @Override
    public int size() {
        return arr.size();
    }
}