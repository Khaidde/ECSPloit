package ecsploit.utils.collections;

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

    public T getObject(int id) {
        int index = sparseList.indexOf(id);
        if (index == -1) return null;
        return denseList.get(index);
    }

    public void addOrReplaceObject(int id, T item) {
        if (sparseList.contains(id)) {
            denseList.fastSet(sparseList.fastIndexOf(id), item);
        } else {
            sparseList.add(id);
            denseList.add(item);
        }
    }

    public T removeObject(int id) {
        T object = denseList.fastRemove(sparseList.fastIndexOf(id));
        sparseList.fastRemove(id);
        return object;
    }

    public boolean contains(int id) {
        return this.sparseList.contains(id);
    }
}
