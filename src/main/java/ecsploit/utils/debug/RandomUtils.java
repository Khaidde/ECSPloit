package ecsploit.utils.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class RandomUtils {

    private static final Random random = new Random();

    private RandomUtils() {}

    public static int[] randIntA(int bound, int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = random.nextInt(bound);
        }
        return arr;
    }

    public static int[] randIntAExclude(int bound, int size, int... exclude) {
        List<Integer> arr = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int value = -1;
            while (value == -1) {
                value = random.nextInt(bound);
                for (int k: exclude) {
                    if (value == k) {
                        value = -1;
                        break;
                    }
                }
                if (value != -1) arr.add(value);
            }
        }
        int[] out = new int[size];
        for (int i = 0; i < size; i++) {
            out[i] = arr.get(i);
        }
        return out;
    }

    public static int[] randIntSetExclude(int bound, int size, int... exclude) {
        if (bound - exclude.length < size) {
            throw new IllegalArgumentException("Set of size=" + size + " can't be generated with bound=" + bound + " and totalExclude=" + exclude.length);
        }
        List<Integer> arr = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int value = -1;
            while (value == -1) {
                value = random.nextInt(bound);
                for (int k: exclude) {
                    if (value == k) {
                        value = -1;
                        break;
                    }
                }
                for (int k: arr) {
                    if (value == k) {
                        value = -1;
                        break;
                    }
                }
                if (value != -1) arr.add(value);
            }
        }
        int[] out = new int[size];
        for (int i = 0; i < size; i++) {
            out[i] = arr.get(i);
        }
        return out;
    }
}
