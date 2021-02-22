package info.kgeorgiy.ja.alyokhin.arrayset;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;

public class ListWithOrder<T> extends AbstractList<T> implements RandomAccess {
    private final List<T> arr;
    private boolean reversed = false;
    public ListWithOrder(List<T> arr) {
        if(arr instanceof ListWithOrder) {
            this.arr = ((ListWithOrder<T>) arr).getArr();
            this.reversed = !((ListWithOrder<Object>) arr).reversed;
        } else {
            this.arr = arr;
        }
    }

    public boolean isReversed() {
        return reversed;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    public List<T> getArr() {
        return arr;
    }

    public int getIndex(int index) {
        if(reversed) {
            return size() - index - 1;
        } else {
            return index;
        }
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