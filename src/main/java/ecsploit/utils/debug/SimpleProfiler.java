package ecsploit.utils.debug;

public class SimpleProfiler {

    private long start;

    public void start() {
        this.start = System.currentTimeMillis();
    }

    public long stop() {
        return System.currentTimeMillis() - start;
    }
}
