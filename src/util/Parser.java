package util;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for parsing puzzle input files.
 */
public class Parser {

    /**
     * Container class for parsed puzzle information.
     */
    public static class ParsedResult {
        public State initialState;
        public int width;
        public int height;
        public int kRow;
        public int kCol;
        public String exitDirection; // "right", "left", "top", "bottom"

        /**
         * Creates a new parsed result with the specified parameters.
         * 
         * @param state Initial state of the puzzle
         * @param width Width of the puzzle grid
         * @param height Height of the puzzle grid
         * @param kRow Row position of the exit
         * @param kCol Column position of the exit
         * @param exitDirection Direction of the exit
         */
        public ParsedResult(State state, int width, int height, int kRow, int kCol, String exitDirection) {
            this.initialState = state;
            this.width = width;
            this.height = height;
            this.kRow = kRow;
            this.kCol = kCol;
            this.exitDirection = exitDirection;
        }
    }

    /**
     * Parses a puzzle file and creates the initial state.
     * 
     * @param filename Path to the puzzle file
     * @return ParsedResult containing initial state and puzzle parameters
     * @throws IOException If file cannot be read
     */
    public static ParsedResult parseFile(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename));
        String[] dims = lines.get(0).trim().split(" ");
        int height = Integer.parseInt(dims[0]);
        int width = Integer.parseInt(dims[1]);
    
        int totalBits = width * height;
        int chunkCount = (totalBits + 63) / 64;
    
        int n = Integer.parseInt(lines.get(1).trim()); // Ga kepake
    
        Map<Character, List<Integer>> carPositions = new HashMap<>();
        
        // Find K position
        int kRow = -1;
        int kCol = -1;
        int kcount = 0;
        String exitDirection = "";
        
        Boolean vk = false;
        int i = 0;
        int j = 0;
        while(i < lines.size()-2) {
            String row = lines.get(i + 2);
            Boolean k = false;
            j = 0;
            while (j < row.length()) {
                char c = row.charAt(j);
                if (c == ' ')
                {
                    if(!(i == 0 && lines.size()-2 > height))
                    {
                        k = true;
                    }
                }
                if (c == 'K') {
                    if(i == 0)
                    {
                        vk = true;
                    } else {
                        k = true;
                    }
                    kRow = i;
                    kCol = j;
                    kcount++;
                    if (kcount > 1) {
                        throw new IllegalArgumentException("Hanya ada satu K yang diperbolehkan");
                    }
                    if(kRow == 0 && kCol == 0 || kRow == width && kCol == 0 || kRow == 0 && kCol == height || kRow == width && kCol == height || ((kRow >  0 && kRow < height && lines.size() == height) && (kCol >  0 && kCol < width && row.length() == width))) {
                        throw new IllegalArgumentException("Lokasi K invalid wak :(");
                    }
                    
                    if (j == 0) exitDirection = "left";
                    else if (j == width) exitDirection = "right";
                    else if (i == 0) exitDirection = "top";
                    else exitDirection = "bottom";
                }
                if (c != '.' && c != 'K' && c != ' ')
                {

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
                j++;
            }
            i++;
        }
        // Validasi jika K gaada
        if (kRow == -1 || kCol == -1) {
            throw new IllegalArgumentException("Mana K yang Kau janjikan itu WOK!!");
        }
    
        // Validate car positions
        for (Map.Entry<Character, List<Integer>> entry : carPositions.entrySet()) {
            char id = entry.getKey();
            List<Integer> positions = entry.getValue();
            
            if (positions.size() > 1) {
                // Check if horizontal (all positions in the same row)
                boolean sameRow = true;
                int firstRow = positions.get(0) / width;
                for (i = 1; i < positions.size(); i++) {
                    if (positions.get(i) / width != firstRow) {
                        sameRow = false;
                        break;
                    }
                }
                
                // Check if vertical (all positions in the same column)
                boolean sameCol = true;
                int firstCol = positions.get(0) % width;
                for (i= 1; i < positions.size(); i++) {
                    if (positions.get(i) % width != firstCol) {
                        sameCol = false;
                        break;
                    }
                }
                
                // Car must be either horizontal or vertical
                if (!sameRow && !sameCol) {
                    throw new IllegalArgumentException("Car " + id + " is neither horizontal nor vertical.");
                }
            }
        }

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