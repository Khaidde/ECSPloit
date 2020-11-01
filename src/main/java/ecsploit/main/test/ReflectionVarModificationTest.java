package ecsploit.main.test;

import ecsploit.main.test.TestGameLib.*;
import ecsploit.utils.debug.SimpleProfiler;

import java.lang.reflect.Field;
import java.util.Random;

public class ReflectionVarModificationTest {

    public static void main(String[] args) {

        Transform transform = new Transform().setPos(-2, 3);

        Field xField = null;
        try {
            xField = transform.getClass().getDeclaredField("x");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        xField.setAccessible(true);

        Field yField = null;
        try {
            yField = transform.getClass().getDeclaredField("y");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        yField.setAccessible(true);

        final int N = 1_000_000;

        Random random = new Random();
        SimpleProfiler profiler = new SimpleProfiler();

        int iterations = 20;
        for (int j = 0; j < iterations; j++) {
            profiler.start();
            for (int i = 0; i < N; i++) {
                transform.setPos(random.nextInt(100), random.nextInt(100));
            }
            if (j == iterations - 1) System.out.println("Regular" + j + " :" + profiler.stop() + " ms");

            profiler.start();
            for (int i = 0; i < N; i++) {
                try {
                    xField.set(transform, random.nextInt(100));
                    yField.set(transform, random.nextInt(100));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            if (j == iterations - 1) System.out.println("Reflection" + j + " :" + profiler.stop() + " ms");
        }
    }
}
