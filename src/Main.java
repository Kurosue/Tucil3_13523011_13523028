import util.Map;
import util.Parser;
import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) {
        Map m = new Map();
        Parser p = new Parser();

        try {
            p.ReadFile(m);
            System.out.println("A = " + m.A);
        } catch (FileNotFoundException e) {
            System.out.println("File tidak ditemukan: " + e.getMessage());
        }
    }
}

