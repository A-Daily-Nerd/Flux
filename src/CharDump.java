import java.nio.file.*;
public class CharDump {
    public static void main(String[] args) throws Exception {
        String s = Files.readString(Path.of("test.flx"));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            System.out.printf("%03d: '%s' (%d)\n", i, c == '\n' ? "\\n" : c == '\r' ? "\\r" : c, (int)c);
        }
    }
}
