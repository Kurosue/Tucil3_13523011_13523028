package util;

import java.util.*;

/**
 * Represents a car in the rush hour puzzle.
 */
public class Car {
    public char id;
    public boolean isHorizontal;
    public int length;
    public long[] bitmask;
    public int col; // Kalau horizontal, nilai -1
    public int row; // Kalau vertical, nilai -1

    /**
     * Creates a new car with the specified parameters.
     * 
     * @param id Unique identifier character for this car
     * @param isHorizontal True if car is horizontally oriented, false for vertical
     * @param length Length of the car in grid cells
     * @param bitmask Bit representation of the car's position on the grid
     * @param col Column index for vertical cars, -1 for horizontal cars
     * @param row Row index for horizontal cars, -1 for vertical cars
     */
    public Car(char id, boolean isHorizontal, int length, long[] bitmask, int col, int row) {
        this.id = id;
        this.isHorizontal = isHorizontal;
        this.length = length;
        this.bitmask = bitmask;
        this.col = col;
        this.row = row;
    }

    /**
     * Enum representing the four possible movement directions.
     */
    public enum Direction {
        LEFT, RIGHT, UP, DOWN
    }

    /**
     * Creates a copy of this car with the same properties.
     * 
     * @return New Car object with the same properties
     */
    public Car copy() {
        return new Car(id, isHorizontal, length, bitmask.clone(), col, row);
    }

    /**
     * Gets the possible movement directions for this car based on orientation.
     * 
     * @return List of valid directions (LEFT/RIGHT for horizontal, UP/DOWN for vertical)
     */
    public List<Direction> getPossibleDirections() {
        return isHorizontal ?
                List.of(Direction.LEFT, Direction.RIGHT) :
                List.of(Direction.UP, Direction.DOWN);
    }

    /**
     * Creates a new car shifted one cell in the specified direction.
     * 
     * @param dir Direction to shift the car
     * @param width Width of the puzzle grid
     * @param height Height of the puzzle grid
     * @return New Car object with updated position, or null if move is invalid
     */
    public Car shift(Direction dir, int width, int height) {
        int totalBits = width * height;
        int chunkCount = (totalBits + 63) / 64;
        long[] shifted = new long[chunkCount];

        int offset = switch (dir) {
            case LEFT -> -1;
            case RIGHT -> 1;
            case UP -> -width;
            case DOWN -> width;
        };
        
        for (int i = 0; i < totalBits; i++) {
            int chunk = i / 64;
            int bit = i % 64;
            if ((bitmask[chunk] & (1L << bit)) != 0) {
                int newIndex = i + offset;

                if (newIndex < 0 || newIndex >= totalBits) return null;

                int newRow = newIndex / width;
                int newCol = newIndex % width;
                int oldRow = i / width;
                int oldCol = i % width;

                if (isHorizontal && newRow != oldRow) return null;
                if (!isHorizontal && newCol != oldCol) return null;

                shifted[newIndex / 64] |= (1L << (newIndex % 64));
            }
        }

        return new Car(id, isHorizontal, length, shifted, col, row);
    }
}
