import util.Map;
import util.Parser;
import java.io.FileNotFoundException;

public class Main {
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

