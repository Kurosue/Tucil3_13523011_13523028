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

        public ParsedResult(State state, int width, int height) {
            this.initialState = state;
            this.width = width;
            this.height = height;
        }
    }

    public static ParsedResult parseFile(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename));
        String[] dims = lines.get(0).trim().split(" ");
        int height = Integer.parseInt(dims[0]);
        int width = Integer.parseInt(dims[1]);

        int totalBits = width * height;
        int chunkCount = (totalBits + 63) / 64;

        int _ = Integer.parseInt(lines.get(1).trim()); // Ga kepake

        Map<Character, List<Integer>> carPositions = new HashMap<>();
        char[][] board = new char[height][width];

        for (int i = 0; i < height; i++) {
            String row = lines.get(i + 2).trim();
            for (int j = 0; j < width; j++) {
                char c = row.charAt(j);
                board[i][j] = c;
                if (c == '.' || c == 'K') continue;

                int idx = i * width + j;
                carPositions.putIfAbsent(c, new ArrayList<>());
                carPositions.get(c).add(idx);
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
        return new ParsedResult(initial, width, height);
    }
}