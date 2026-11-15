import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public final class Main {
    private static final Path DEFAULT_FILE = Path.of("benchmark", "harry.txt");
    private static final int DEFAULT_LIMIT = 30;
    private static final int BUFFER_SIZE = 8192;
    private static final Comparator<Map.Entry<String, Integer>> BEST_FIRST = Comparator.<Map.Entry<String, Integer>>comparingInt(
            Map.Entry::getValue)
            .reversed().thenComparing(Map.Entry::getKey);

    private Main() {
    }

    public static void main(String[] args) {
        long started = System.nanoTime();
        try {
            if (args.length > 2)
                throw new IllegalArgumentException("Usage: java Main [file-path] [positive-limit]");
            Path path = args.length >= 1 ? Path.of(args[0]) : DEFAULT_FILE;
            int limit = args.length == 2 ? Integer.parseInt(args[1]) : DEFAULT_LIMIT;
            if (limit <= 0)
                throw new IllegalArgumentException("Limit must be positive");
            Map<String, Integer> counts = countWords(path);
            for (Map.Entry<String, Integer> entry : topWords(counts, limit)) {
                System.out.printf("%s %d%n", entry.getKey(), entry.getValue());
            }
            System.out.println("------");
            System.out.printf("Elapsed: %.3f ms%n", (System.nanoTime() - started) / 1_000_000.0);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    public static Map<String, Integer> countWords(Path path) throws IOException {
        if (path == null)
            throw new IllegalArgumentException("Path must not be null");
        Map<String, Integer> counts = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            char[] buffer = new char[BUFFER_SIZE];
            StringBuilder word = new StringBuilder(32);
            int read;
            while ((read = reader.read(buffer)) != -1) {
                for (int i = 0; i < read; i++) {
                    char ch = buffer[i];
                    if (ch >= 'A' && ch <= 'Z')
                        ch = (char) (ch + ('a' - 'A'));
                    if (ch >= 'a' && ch <= 'z') {
                        word.append(ch);
                    } else if (word.length() > 0) {
                        counts.merge(word.toString(), 1, Integer::sum);
                        word.setLength(0);
                    }
                }
            }
            if (word.length() > 0)
                counts.merge(word.toString(), 1, Integer::sum);
        }
        return counts;
    }

    public static List<Map.Entry<String, Integer>> topWords(Map<String, Integer> counts, int limit) {
        if (counts == null || limit < 0)
            throw new IllegalArgumentException("Invalid counts or limit");
        if (limit == 0)
            return List.of();
        PriorityQueue<Map.Entry<String, Integer>> heap = new PriorityQueue<>(
                Math.min(limit, counts.size()) + 1, BEST_FIRST.reversed());
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (heap.size() < limit)
                heap.offer(entry);
            else if (BEST_FIRST.compare(entry, heap.peek()) < 0) {
                heap.poll();
                heap.offer(entry);
            }
        }
        List<Map.Entry<String, Integer>> result = new ArrayList<>(heap);
        result.sort(BEST_FIRST);
        return result;
    }
}
