package util;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Parser {
    public void ReadFile(Map m) throws FileNotFoundException {
        Scanner inputScanner = new Scanner(System.in);
        System.out.print("Enter file name: ");
        String fileName = inputScanner.nextLine();
        
        File file = new File(fileName);
        Scanner fileScanner = new Scanner(file);

        String res = fileScanner.nextLine();
        String[] tokens = res.trim().split("\\s+");
        int A = Integer.parseInt(tokens[0]);
        int B = Integer.parseInt(tokens[1]);

        res = fileScanner.nextLine();
        int N = Integer.parseInt(res);
        m.setMapAttr(A, B, N);

        char[][] tempMap = new char[A + 1][B + 1];
        for (int i = 0; i < A + 1 && fileScanner.hasNextLine(); i++) {
            String line = fileScanner.nextLine();
            for (int j = 0; j < B + 1 && j < line.length(); j++) {
                tempMap[i][j] = line.charAt(j);
            }
        }

        m.setMap(tempMap);
        fileScanner.close();
    }
}

