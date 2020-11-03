package ecsploit.utils.collections;

import ecsploit.utils.debug.ToStringBuilder;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Efficient data structure for iteration. Similar to arraylist but with no guaranteed order.
 *
 * @param <T> type of object contained in the list
 */
public class DenseList<T> {
	
	private int size;
	private T[] data;

	public DenseList() {
		this(64);
	}

	@SuppressWarnings("unchecked")
	public DenseList(int initialSize) {
		this.size = 0;
		this.data = (T[]) new Object[initialSize];
	}
	
	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Quickly retrieve with no bounds checking. Use with caution.
	 * @throws ArrayIndexOutOfBoundsException if index is larger than max data capacity
	 *
	 * @param index index of data array to be retrieved
	 * @return the object at the index of the data array or NULL if object does not exist at the index
	 */
	public T fastGet(int index) {
		return data[index];
	}

	public T get(int index) {
		if (index >= data.length) return null;
		return this.fastGet(index);
	}

	/**
	 * Quickly set the index in the array to be a certain value. No bounds checking. Use sparingly with caution.
	 * <p>
	 *     Note: Set functions should only be used to replace existing values as to maintain "denseness" of the dense
	 *     list data structure.
	 * </p>
	 * @throws ArrayIndexOutOfBoundsException if index is larger than max data capacity
	 *
	 * @param index arbitrary position in array to set
	 * @param item object to assign
	 */
	public void fastSet(int index, T item) {
		this.data[index] = item;
	}

	public void set(int index, T item) {
		if (index >= this.data.length) this.data = CollectionUtils.grow(this.data);
		this.data[index] = item;
	}

	public void add(T item) {
		if (this.size >= this.data.length) this.data = CollectionUtils.grow(this.data);
		this.data[size++] = item;
	}

	/**
	 * Quickly remove removal item without bounds checking
	 * @throws ArrayIndexOutOfBoundsException if index is outside bounds of data array
	 *
	 * @param index index of data array to be removed
	 * @return the object at the index of the data array
	 */
	public T fastRemove(int index) {
		T item = data[index];
		data[index] = data[--size];
		data[size] = null;
		return item;
	}

	public T remove(int index) {
		if (index >= data.length) return null;
		T item = data[index];
		data[index] = data[--size];
		data[size] = null;
		return item;
	}
	
	public T removeLast() {
		if (size > 0) {
			return this.remove(size - 1);
		}
		return null;
	}
	
	public void clear() {
		Arrays.fill(data, 0, size, null);
		this.size = 0;
	}

	public void forEach(Consumer<T> action) {
		if (this.isEmpty()) return;
		for (int i = 0; i < size; i++) {
			action.accept(data[i]);
		}
	}

	public T[] data() {
		return Arrays.copyOf(data, size);
	}

	public String toString() {
		return ToStringBuilder.from(this)
				.withArray("data", Arrays.copyOf(data, size))
				.toString();
	}
}
