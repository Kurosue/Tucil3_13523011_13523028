package test;

import util.Car;
import util.Car.Direction;

public class Shifting {
    public static void main(String[] args) {
        // Test parameters
        int width = 6;
        int height = 6;
        int totalBits = width * height;
        int chunkCount = (totalBits + 63) / 64;
        
        // Test 1: Horizontal car (length 2)
        System.out.println("=== Test 1: Horizontal Car (Length 2) ===");
        // Create a car at position (0,0) and (0,1) - "AA" at top left
        long[] bitmask1 = new long[chunkCount];
        bitmask1[0] |= (1L << 0);  // Position (0,0)
        bitmask1[0] |= (1L << 1);  // Position (0,1)
        Car horizontalCar = new Car('A', true, 2, bitmask1, -1, 0);
        
        printCarPosition(horizontalCar, width, height);
        
        // Test moving right
        Car rightShifted = horizontalCar.shift(Direction.RIGHT, width, height);
        System.out.println("\nAfter moving RIGHT:");
        printCarPosition(rightShifted, width, height);
        
        // Test moving left
        Car leftShifted = horizontalCar.shift(Direction.LEFT, width, height);
        System.out.println("\nAfter moving LEFT (should be null - out of bounds):");
        if (leftShifted == null) {
            System.out.println("Correctly returned null - car would go out of bounds");
        } else {
            printCarPosition(leftShifted, width, height);
        }
        
        // Test 2: Vertical car (length 3)
        System.out.println("\n=== Test 2: Vertical Car (Length 3) ===");
        // Create a car at positions (1,1), (2,1), (3,1) - vertical car
        long[] bitmask2 = new long[chunkCount];
        bitmask2[0] |= (1L << (1 * width + 1));  // Position (1,1)
        bitmask2[0] |= (1L << (2 * width + 1));  // Position (2,1)
        bitmask2[0] |= (1L << (3 * width + 1));  // Position (3,1)
        Car verticalCar = new Car('B', false, 3, bitmask2, 1, -1);
        
        printCarPosition(verticalCar, width, height);
        
        // Test moving up
        Car upShifted = verticalCar.shift(Direction.UP, width, height);
        System.out.println("\nAfter moving UP:");
        printCarPosition(upShifted, width, height);
        
        // Test moving down
        Car downShifted = verticalCar.shift(Direction.DOWN, width, height);
        System.out.println("\nAfter moving DOWN:");
        printCarPosition(downShifted, width, height);
        
        // Test illegal moves
        Car rightIllegal = verticalCar.shift(Direction.RIGHT, width, height);
        System.out.println("\nAfter trying to move a vertical car RIGHT (should be allowed but breaks car):");
        printCarPosition(rightIllegal, width, height);
    }
    
    private static void printCarPosition(Car car, int width, int height) {
        if (car == null) {
            System.out.println("Car is null");
            return;
        }
        
        System.out.println("Car '" + car.id + "' positions:");
        
        char[][] board = new char[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                board[i][j] = '.';
            }
        }
        
        // Fill the board with car positions
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int idx = i * width + j;
                if ((car.bitmask[idx / 64] & (1L << (idx % 64))) != 0) {
                    board[i][j] = car.id;
                }
            }
        }
        
        // Print the board
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }
}