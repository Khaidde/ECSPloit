package ecsploit.utils.collections;

import ecsploit.utils.debug.ToStringBuilder;

import java.util.Arrays;
import java.util.Collections;

/**
 * Efficient data structure for adding, removing and retrieving integers in constant time.
 */
public class SparseList {

    private int size;
    private int[] innerList;
    private int[] outerList;

    public SparseList() {
        this(64);
    }

    public SparseList(int initialSize) {
        this.size = 0;
        this.innerList = new int[initialSize];
        this.outerList = new int[initialSize];
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Quickly retrieve id with no bounds checking. Use with caution.
     * @throws ArrayIndexOutOfBoundsException if index is larger than max innerList capacity
     *
     * @param index index of innerList to be retrieved
     * @return the id at the index of the innerList array or an undefined case(typically 0) when id value does not exist
     */
    public int fastGet(int index) {
        return this.innerList[index];
    }

    /**
     * Safe but slightly slower id retrieval method which first checks bounds.
     *
     * @param index index of innerList to be retrieved
     * @return the id at the index of the innerList array or -1 when index is not valid
     */
    public int get(int index) {
        if (index >= this.size) return -1;
        return this.fastGet(index);
    }

    /**
     * Quickly retrieve index with no bounds checking. Use with caution.
     * @throws ArrayIndexOutOfBoundsException if id is larger than max outerList capacity
     *
     * @param id value to be indexed from outerList
     * @return the index of the id value in the innerList or 0 in undefined cases where id value does not exist
     */
    public int fastIndexOf(int id) {
        return this.outerList[id];
    }

    /**
     * Safe but slightly slower index retrieval method which first checks bounds.
     *
     * @param id value to be indexed from outerList
     * @return the index of the id value in the innerList or -1 where id value does not exist
     */
    public int indexOf(int id) {
        if (!this.contains(id)) return -1;
        return this.fastIndexOf(id);
    }

    public void add(int id) {
        if (this.size >= this.innerList.length) {
            this.innerList = Arrays.copyOf(this.innerList, CollectionUtils.getGrowLength(this.size));
        }
        if (id >= this.outerList.length) {
            this.outerList = Arrays.copyOf(this.outerList, CollectionUtils.getGrowLength(id));
        }
        this.innerList[size] = id;
        this.outerList[id] = size;
        this.size++;
    }

    /**
     * Quickly remove an id from the list. Does not check whether or not list contains the id and in this case, it can
     * lead to undefined behavior. Use with extreme caution.
     *
     * @param id value to be removed from the list
     */
    public void fastRemove(int id) {
        int index = this.innerList[--size];
        this.innerList[this.outerList[id]] = index;
        this.outerList[index] = this.outerList[id];
    }

    public boolean contains(int id) {
        if (id >= this.outerList.length) return false;
        int index = this.outerList[id];
        return index < this.size && this.innerList[index] == id;
    }

    public void clear() {
        this.size = 0;
    }

    public String toString() {
        return ToStringBuilder.from(this)
                .withList("innerList", Collections.singletonList(Arrays.copyOf(this.innerList, size)))
                .toString();
    }
}
