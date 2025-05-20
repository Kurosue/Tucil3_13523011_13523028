package util;

import java.util.Map;

/**
 * Utility class for printing puzzle board states.
 */
public class BoardPrinter {

    /**
     * Prints a textual representation of the board state to standard output.
     * 
     * @param state Current state of the puzzle
     * @param width Width of the puzzle grid
     * @param height Height of the puzzle grid
     */
    public static void printBoard(State state, int width, int height) {
        char[][] board = new char[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                board[i][j] = '.';
            }
        }


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
                        if (row < height && col < width && (board[row][col] == '.' || board[row][col] == 'K'))
                            board[row][col] = carId;
                    }
                }
            }
        }

        System.out.println("+" + "-".repeat(width) + "+");
        for (int i = 0; i < height; i++) {
            System.out.print("|");
            for (int j = 0; j < width; j++) {
                System.out.print(board[i][j]);
            }
            System.out.println("|");
        }
        System.out.println("+" + "-".repeat(width) + "+");
    }
    
    /**
     * Prints a textual representation of the board state with an exit marker.
     * 
     * @param state Current state of the puzzle
     * @param width Width of the puzzle grid
     * @param height Height of the puzzle grid
     * @param exitRow Row position of the exit
     * @param exitCol Column position of the exit
     */
    public static void printBoard(State state, int width, int height, int exitRow, int exitCol) {
        char[][] board = new char[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                board[i][j] = '.';
            }
        }

        if (exitRow >= 0 && exitRow < height && exitCol >= 0 && exitCol < width) {
            board[exitRow][exitCol] = 'K';
        }

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
                        if (row < height && col < width && (board[row][col] == '.' || board[row][col] == 'K'))
                            board[row][col] = carId;
                    }
                }
            }
        }

        System.out.println("+" + "-".repeat(width) + "+");
        for (int i = 0; i < height; i++) {
            System.out.print("|");
            for (int j = 0; j < width; j++) {
                System.out.print(board[i][j]);
            }
            System.out.println("|");
        }
        System.out.println("+" + "-".repeat(width) + "+");
    }
}