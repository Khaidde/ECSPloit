package ecsploit.utils.collections;

import java.util.function.Consumer;

/**
 * Sparse list with reference to dense list of objects.
 * <p>
 *     Note: most of the functions in this class don't have bound checking.
 * </p>
 */
public class PackedObjectList<T> {

    private final DenseList<T> denseList;
    private final SparseList sparseList;

    public PackedObjectList() {
        this.denseList = new DenseList<>();
        this.sparseList = new SparseList();
    }

    public PackedObjectList(int initialSize) {
        this.denseList = new DenseList<>(initialSize);
        this.sparseList = new SparseList(initialSize);
    }

    public int size() {
        return this.denseList.size();
    }

    public T getObject(int id) {
        int index = sparseList.indexOf(id);
        if (index == -1) return null;
        return denseList.get(index);
    }

    public void setObject(int id, T item) {
        if (sparseList.contains(id)) {
            denseList.fastSet(sparseList.fastIndexOf(id), item);
        } else {
            sparseList.add(id);
            denseList.add(item);
        }
    }

    public int addObject(T item) {
        int id = this.sparseList.size();
        sparseList.add(id);
        denseList.add(item);
        return id;
    }

    public T removeObject(int id) {
        int index = sparseList.indexOf(id);
        if (index == -1) return null;
        T object = denseList.fastRemove(index);
        sparseList.fastRemove(id);
        return object;
    }

    public boolean contains(int id) {
        return this.sparseList.contains(id);
    }

    public void forEach(Consumer<T> action) {
        this.denseList.forEach(action);
    }
}
