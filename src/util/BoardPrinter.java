package util;

import java.util.Map;

public class BoardPrinter {

    public static void printBoard(State state, int width, int height, long[] goalMask) {
        char[][] board = new char[height][width];

        // Initialize all cells to '.'
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                board[i][j] = '.';
            }
        }

        // Set goal cell (K)
        for (int i = 0; i < goalMask.length; i++) {
            long chunk = goalMask[i];
            for (int b = 0; b < 64; b++) {
                if ((chunk & (1L << b)) != 0) {
                    int idx = i * 64 + b;
                    int row = idx / width;
                    int col = idx % width;
                    if (row < height && col < width)
                        board[row][col] = 'K';
                }
            }
        }

        // Set each car on the board
        for (Map.Entry<Character, Car> entry : state.cars.entrySet()) {
            char carId = entry.getKey();
            Car car = entry.getValue();

            for (int i = 0; i < car.bitmask.length; i++) {
                long chunk = car.bitmask[i];
                for (int b = 0; b < 64; b++) {
                    if ((chunk & (1L << b)) != 0) {
                        int idx = i * 64 + b;
                        int row = idx / width;
                        int col = idx % width;
                        if (row < height && col < width && board[row][col] == '.')
                            board[row][col] = carId;
                    }
                }
            }
        }

        // Print board
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.out.print(board[i][j]);
            }
            System.out.println();
        }
    }
}
