package ecsploit.utils.collections;

public class RingBuffer<T> {

    private final T[] buffer;
    private int writePointer;
    private int readPointer;

    @SuppressWarnings("unchecked")
    public RingBuffer(int size) {
        this.buffer = (T[]) new Object[size];
        this.writePointer = 0;
        this.readPointer = 0;
    }

    public int getWritePos() {
        return writePointer;
    }

    public int getReadPos() {
        return readPointer;
    }

    public int size() {
        return writePointer - readPointer;
    }

    public T get(int index) {
        return buffer[index % buffer.length];
    }

    public void push(T object) {
        buffer[writePointer++ % buffer.length] = object;
    }

    public T fastPoll() {
        return buffer[readPointer++ % buffer.length];
    }

    public T poll() {
        if (this.size() <= 0) return null;
        return buffer[readPointer++ % buffer.length];
    }

}
