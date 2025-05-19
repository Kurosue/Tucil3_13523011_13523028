package util;

import java.util.*;

public class Car {
    public char id;
    public boolean isHorizontal;
    public int length;
    public long[] bitmask;

    public Car(char id, boolean isHorizontal, int length, long[] bitmask) {
        this.id = id;
        this.isHorizontal = isHorizontal;
        this.length = length;
        this.bitmask = bitmask;
    }

    public enum Direction {
    LEFT, RIGHT, UP, DOWN
    }

    public Car copy() {
        return new Car(id, isHorizontal, length, bitmask.clone());
    }

    public List<Direction> getPossibleDirections() {
        return isHorizontal ?
                List.of(Direction.LEFT, Direction.RIGHT) :
                List.of(Direction.UP, Direction.DOWN);
    }

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

        return new Car(id, isHorizontal, length, shifted);
    }
}
