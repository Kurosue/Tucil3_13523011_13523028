package heuristic;

import util.Car;
import util.State;

/**
 * Simple heuristic that calculates the distance from the primary car to the exit.
 */
public class Distance implements Heuristic {
    
    /**
     * Returns the name of this heuristic function.
     * 
     * @return String name of the heuristic
     */
    @Override
    public String getName() {
        return "Distance to Exit";
    }
    
    /**
     * Calculates the direct distance from the primary car to the exit edge.
     * 
     * @param state The current puzzle state
     * @param width Width of the puzzle grid
     * @param height Height of the puzzle grid
     * @param exitDirection Direction of the exit ("left", "right", "top", "bottom")
     * @return Distance to exit or MAX_VALUE if car orientation is incompatible with exit
     */
    @Override
    public int calculate(State state, int width, int height, String exitDirection) {
        Car primaryCar = state.cars.get('P');
        if (primaryCar == null) return Integer.MAX_VALUE;
        
        if (primaryCar.isHorizontal) {
            // For horizontal car, check if exit is horizontal
            if (!("right".equals(exitDirection) || "left".equals(exitDirection))) {
                return Integer.MAX_VALUE; // Incompatible exit direction
            }
            
            // Find car's positions
            int leftmostCol = findLeftmostColumn(primaryCar, width);
            int rightmostCol = findRightmostColumn(primaryCar, width);
            
            // Calculate distance to appropriate edge based on exit direction
            if ("right".equals(exitDirection)) {
                return width - 1 - rightmostCol; // Distance to right edge
            } else { // left
                return leftmostCol; // Distance to left edge
            }
        } else { // Vertical car
            // For vertical car, check if exit is vertical
            if (!("top".equals(exitDirection) || "bottom".equals(exitDirection))) {
                return Integer.MAX_VALUE; // Incompatible exit direction
            }
            
            // Find car's positions
            int topmostRow = findTopmostRow(primaryCar, width);
            int bottommostRow = findBottommostRow(primaryCar, width);
            
            // Calculate distance to appropriate edge based on exit direction
            if ("bottom".equals(exitDirection)) {
                return height - 1 - bottommostRow; // Distance to bottom edge
            } else { // top
                return topmostRow; // Distance to top edge
            }
        }
    }
    
    /**
     * Determines the leftmost column occupied by a car.
     * 
     * @param car The car to analyze
     * @param width Width of the puzzle grid
     * @return Index of the leftmost column
     */
    public static int findLeftmostColumn(Car car, int width) {
        int leftmost = Integer.MAX_VALUE;
        for (int chunk = 0; chunk < car.bitmask.length; chunk++) {
            long bits = car.bitmask[chunk];
            if (bits == 0) continue; // Skip if no bits set in this chunk
            
            for (int bit = 0; bit < 64; bit++) {
                if ((bits & (1L << bit)) != 0) {
                    int index = chunk * 64 + bit;
                    int col = index % width;
                    leftmost = Math.min(leftmost, col);
                }
            }
        }
        return leftmost;
    }
    
    /**
     * Determines the rightmost column occupied by a car.
     * 
     * @param car The car to analyze
     * @param width Width of the puzzle grid
     * @return Index of the rightmost column
     */
    public static int findRightmostColumn(Car car, int width) {
        int rightmost = -1;
        for (int chunk = 0; chunk < car.bitmask.length; chunk++) {
            long bits = car.bitmask[chunk];
            if (bits == 0) continue; // Skip if no bits set in this chunk
            
            for (int bit = 0; bit < 64; bit++) {
                if ((bits & (1L << bit)) != 0) {
                    int index = chunk * 64 + bit;
                    int col = index % width;
                    rightmost = Math.max(rightmost, col);
                }
            }
        }
        return rightmost;
    }
    
    /**
     * Determines the topmost row occupied by a car.
     * 
     * @param car The car to analyze
     * @param width Width of the puzzle grid
     * @return Index of the topmost row
     */
    public static int findTopmostRow(Car car, int width) {
        int topmost = Integer.MAX_VALUE;
        for (int chunk = 0; chunk < car.bitmask.length; chunk++) {
            long bits = car.bitmask[chunk];
            if (bits == 0) continue; // Skip if no bits set in this chunk
            
            for (int bit = 0; bit < 64; bit++) {
                if ((bits & (1L << bit)) != 0) {
                    int index = chunk * 64 + bit;
                    int row = index / width;
                    topmost = Math.min(topmost, row);
                }
            }
        }
        return topmost;
    }
    
    /**
     * Determines the bottommost row occupied by a car.
     * 
     * @param car The car to analyze
     * @param width Width of the puzzle grid
     * @return Index of the bottommost row
     */
    public static int findBottommostRow(Car car, int width) {
        int bottommost = -1;
        for (int chunk = 0; chunk < car.bitmask.length; chunk++) {
            long bits = car.bitmask[chunk];
            if (bits == 0) continue; // Skip if no bits set in this chunk
            
            for (int bit = 0; bit < 64; bit++) {
                if ((bits & (1L << bit)) != 0) {
                    int index = chunk * 64 + bit;
                    int row = index / width;
                    bottommost = Math.max(bottommost, row);
                }
            }
        }
        return bottommost;
    }
}