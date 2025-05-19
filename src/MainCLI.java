import java.io.FileNotFoundException;
import util.Map;
import util.Parser;

public class MainCLI {
    public static void main(String[] args) {
        Map m = new Map();
        Parser p = new Parser();

        // Parser file
        try {
            p.ReadFile(m);
        } catch (FileNotFoundException e) {
            System.out.println("File tidak ditemukan: " + e.getMessage());
        }
    }
}

