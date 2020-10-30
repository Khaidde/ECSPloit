package ecsploit.utils.collections;

import java.util.Arrays;

public class DenseQueue<T> {

    private T[] data;
    private int writePointer;
    private int readPointer;

    public DenseQueue() {
        this(64);
    }

    @SuppressWarnings("unchecked")
    public DenseQueue(int initialSize) {
        this.writePointer = 0;
        this.readPointer = 0;
        this.data = (T[]) new Object[initialSize];
    }

    public int size() {
        return writePointer - readPointer;
    }

    public void push(T item) {
        if (this.writePointer >= this.data.length) this.data = CollectionUtils.grow(this.data);
        this.data[writePointer++] = item;
    }

    public T poll() {
        if (this.size() <= 0) return null;
        return this.data[readPointer++];
    }

    public void reset() {
        this.writePointer = 0;
        this.readPointer = 0;
    }

    public String toString() {
        return "DenseQueue:" + Arrays.asList(Arrays.copyOfRange(data, readPointer, writePointer));
    }
}
