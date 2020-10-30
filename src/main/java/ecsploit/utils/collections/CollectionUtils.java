package ecsploit.utils.collections;

import java.util.Arrays;

public final class CollectionUtils {

    private CollectionUtils() {}

    /**
     * Creates a new array based off of the length of the array and copies content of original array. Grows by roughly
     * 1.5x the original size.
     *
     * @param data the array of objects to be reassigned
     * @param <T> type of data array
     * @return NEW array instance with increased size
     */
    public static <T> T[] grow(T[] data) {
        return Arrays.copyOf(data, getGrowLength(data.length));
    }

    /**
     * Similar to CollectionUtils.grow() but size can be specified. Unsafe if original size is significantly less than
     * the actual size of the data array: Can lead to deletion of data since not all elements are copied.
     *
     * @param data the array of objects to be reassigned
     * @param originalSize original size to be calculated in expanded array length
     * @param <T> type of data array
     * @return NEW array instance with increased size
     */
    public static <T> T[] growFromSize(T[] data, int originalSize) {
        return Arrays.copyOf(data, getGrowLength(originalSize));
    }

    public static int getGrowLength(int originalSize) {
        return originalSize + (originalSize >>> 1) + 1;
    }

}
