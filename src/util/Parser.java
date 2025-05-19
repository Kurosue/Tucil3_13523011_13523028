package util;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {

    public static class ParsedResult {
        public State initialState;
        public int width;
        public int height;
        public int kRow;
        public int kCol;
        public String exitDirection; // "right", "left", "top", "bottom"

        public ParsedResult(State state, int width, int height, int kRow, int kCol, String exitDirection) {
            this.initialState = state;
            this.width = width;
            this.height = height;
            this.kRow = kRow;
            this.kCol = kCol;
            this.exitDirection = exitDirection;
        }
    }

    public static ParsedResult parseFile(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename));
        String[] dims = lines.get(0).trim().split(" ");
        int height = Integer.parseInt(dims[0]);
        int width = Integer.parseInt(dims[1]);
    
        int totalBits = width * height;
        int chunkCount = (totalBits + 63) / 64;
    
        int n = Integer.parseInt(lines.get(1).trim()); // Ga kepake
    
        Map<Character, List<Integer>> carPositions = new HashMap<>();
        char[][] board = new char[height][width];
        
        // Find K position
        int kRow = -1;
        int kCol = -1;
        String exitDirection = "";
        
        Boolean vk = false;
        for (int i = 0; i < lines.size()-2; i++) {
            String row = lines.get(i + 2);
            Boolean k = false;
            for (int j = 0; j < row.length(); j++) {
                char c = row.charAt(j);
                if (c == ' ') continue;
                
                if (c == 'K') {
                    if(i == 0)
                    {
                        vk = true;
                    } else {
                        k = true;
                    }
                    kRow = i;
                    kCol = j;
                    
                    if (j == 0) exitDirection = "left";
                    else if (j == width) exitDirection = "right";
                    else if (i == 0) exitDirection = "top";
                    else exitDirection = "bottom";
                    
                    continue;
                }
                else{
                    if(k)
                    {
                        board[i][j-1] = c;
                    } else if(vk)
                    {
                        board[i-1][j] = c;
                    } else
                    {
                        board[i][j] = c;
                    }
                }
                if (c == '.' || c == 'K') continue;
                int idx = 0;
                if(k)
                {
                    idx = i * width + (j - 1);
                } else if(vk)
                {
                    idx = (i - 1) * width + j;
                } else{
                    idx = i * width + j;
                }
                carPositions.putIfAbsent(c, new ArrayList<>());
                carPositions.get(c).add(idx);
            }
        }
    
        // Rest of parsing code remains the same
        Map<Character, Car> cars = new HashMap<>();
        for (Map.Entry<Character, List<Integer>> entry : carPositions.entrySet()) {
            char id = entry.getKey();
            List<Integer> positions = entry.getValue();
            int len = positions.size();
    
            boolean horizontal = false;
            if (len > 1) {
                int first = positions.get(0);
                int second = positions.get(1);
                horizontal = (first / width) == (second / width);
            }
    
            long[] bitmask = new long[chunkCount];
            for (int index : positions) {
                bitmask[index / 64] |= (1L << (index % 64));
            }
            
            int row = -1;  // Default -1 untuk mobil horizontal
            int col = -1;  // Default -1 untuk mobil vertikal
            
            if (horizontal) {
                int firstPos = positions.get(0);
                row = firstPos / width;
            } else {
                int firstPos = positions.get(0);
                col = firstPos % width;
            }
    
            cars.put(id, new Car(id, horizontal, len, bitmask, col, row));
        }
    
        State initial = new State(cars, null, "", 0);
        System.out.println(exitDirection);
        return new ParsedResult(initial, width, height, kRow, kCol, exitDirection);
    }
}