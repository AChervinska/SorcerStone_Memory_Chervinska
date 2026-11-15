import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class MainTest {
    public static void main(String[] args) throws Exception {
        Path file = Files.createTempFile("sorcerstone", ".txt");
        try {
            Files.writeString(file, "Harry, HARRY! harry. he the he don't re-enter\nA");
            Map<String, Integer> actual = Main.countWords(file);
            Map<String, Integer> previous = PreviousMain.countWords(Files.readString(file));
            assert actual.equals(previous) : "Optimized counts differ from previous implementation";
            assert actual.get("harry") == 3;
            assert actual.get("he") == 2;
            assert actual.get("the") == 1;
            assert Main.topWords(Map.of("z", 2, "a", 2, "b", 1), 2).get(0).getKey().equals("a");
            assert Main.topWords(Map.of(), 30).isEmpty();
            assert Main.topWords(actual, 0).isEmpty();
            Files.writeString(file, "!!! 123");
            assert Main.countWords(file).isEmpty();
            Files.writeString(file, "");
            assert Main.countWords(file).isEmpty();
            Files.writeString(file, "a".repeat(9000) + " B");
            assert Main.countWords(file).get("a".repeat(9000)) == 1 : "Word across buffer boundary";
            boolean invalid = false;
            try { Main.topWords(Map.of(), -1); } catch (IllegalArgumentException e) { invalid = true; }
            assert invalid;
            System.out.println("All tests passed.");
        } finally { Files.deleteIfExists(file); }
    }
}
