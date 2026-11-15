import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class Benchmark {
    public static void main(String[] args) throws Exception {
        if (args.length != 2 || !(args[0].equals("previous") || args[0].equals("optimized"))) {
            throw new IllegalArgumentException("Usage: Benchmark previous|optimized file-path");
        }
        Path file = Path.of(args[1]);
        System.gc();
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool.getType() == MemoryType.HEAP && pool.isUsageThresholdSupported()) {
            }
            if (pool.getType() == MemoryType.HEAP)
                pool.resetPeakUsage();
        }
        long start = System.nanoTime();
        Map<String, Integer> counts = args[0].equals("previous")
                ? PreviousMain.countWords(Files.readString(file))
                : Main.countWords(file);
        List<Map.Entry<String, Integer>> top = args[0].equals("previous")
                ? PreviousMain.topWords(counts, 30)
                : Main.topWords(counts, 30);
        double millis = (System.nanoTime() - start) / 1_000_000.0;
        long peak = 0;
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool.getType() == MemoryType.HEAP)
                peak += pool.getPeakUsage().getUsed();
        }
        System.out.printf("mode=%s elapsed_ms=%.3f peak_heap_mib=%.3f unique=%d top=%s%n",
                args[0], millis, peak / 1048576.0, counts.size(), top.isEmpty() ? "-" : top.get(0));
    }
}
