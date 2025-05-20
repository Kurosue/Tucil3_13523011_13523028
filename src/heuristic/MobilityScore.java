package heuristic;

import java.util.*;
import util.Car;
import util.State;

/**
 * Heuristic that evaluates states based on car mobility (available moves).
 */
public class MobilityScore implements Heuristic {
    
    /**
     * Returns the name of this heuristic function.
     * 
     * @return String name of the heuristic
     */
    @Override
    public String getName() {
        return "Mobility Score";
    }
    
    /**
     * Calculates a heuristic value based on mobility of cars in the puzzle.
     * Higher mobility (more available moves) results in a lower heuristic value.
     * 
     * @param state The current puzzle state
     * @param width Width of the puzzle grid
     * @param height Height of the puzzle grid
     * @param exitDirection Direction of the exit ("left", "right", "top", "bottom")
     * @return Heuristic value based on car mobility or MAX_VALUE if incompatible with exit
     */
    @Override
    public int calculate(State state, int width, int height, String exitDirection) {
        // Get primary car
        Car primaryCar = state.cars.get('P');
        if (primaryCar == null) return Integer.MAX_VALUE;
        
        // Check exit orientation compatibility
        boolean isExitCompatible = primaryCar.isHorizontal && 
            ("right".equals(exitDirection) || "left".equals(exitDirection)) ||
            !primaryCar.isHorizontal && 
            ("top".equals(exitDirection) || "bottom".equals(exitDirection));
            
        if (!isExitCompatible) return Integer.MAX_VALUE;
        
        // Calculate how many moves each car can make
        int totalAvailableMoves = 0;
        int primaryCarMoves = 0;
        
        for (Car car : state.cars.values()) {
            int moveCount = countPossibleMoves(car, state, width, height);
            if (car.id == 'P') {
                primaryCarMoves = moveCount;
            }
            totalAvailableMoves += moveCount;
        }
        
        // Basic distance heuristic to ensure admissibility
        int distanceValue;
        if (primaryCar.isHorizontal) {
            int leftmostCol = Distance.findLeftmostColumn(primaryCar, width);
            int rightmostCol = Distance.findRightmostColumn(primaryCar, width);
            
            distanceValue = "right".equals(exitDirection) ? 
                (width - 1 - rightmostCol) : leftmostCol;
        } else {
            int topmostRow = Distance.findTopmostRow(primaryCar, width);
            int bottommostRow = Distance.findBottommostRow(primaryCar, width);
            
            distanceValue = "bottom".equals(exitDirection) ? 
                (height - 1 - bottommostRow) : topmostRow;
        }
        
        // Adjust scoring based on mobility - less mobility = higher score (worse)
        // Max cars is typically around 10-12, so normalize to that range
        int expectedMaxCars = 12; 
        
        // If primary car can't move, heavily penalize
        if (primaryCarMoves == 0) {
            return distanceValue + expectedMaxCars * 2;
        }
        
        return distanceValue + (expectedMaxCars - (totalAvailableMoves / 2));
    }
    
    /**
     * Counts how many possible moves a car can make in its current position.
     * For horizontal cars, checks left and right movements.
     * For vertical cars, checks up and down movements.
     * 
     * @param car The car to evaluate
     * @param state Current puzzle state
     * @param width Width of the puzzle grid
     * @param height Height of the puzzle grid
     * @return Number of possible moves the car can make
     */
    private int countPossibleMoves(Car car, State state, int width, int height) {
        int moveCount = 0;
        
        if (car.isHorizontal) {
            for (int offset = 1; ; offset++) {
                int leftmostCol = Distance.findLeftmostColumn(car, width);
                int checkCol = leftmostCol - offset;
                
                if (checkCol < 0) break;
                
                int idx = car.row * width + checkCol;
                int chunk = idx / 64;
                int bit = idx % 64;
                
                if (chunk < state.occupied.length && 
                    (state.occupied[chunk] & (1L << bit)) != 0) {
                    break;
                }
                
                moveCount++;
            }
            
            // Check right
            for (int offset = 1; ; offset++) {
                int rightmostCol = Distance.findRightmostColumn(car, width);
                int checkCol = rightmostCol + offset;
                
                if (checkCol >= width) break;
                
                int idx = car.row * width + checkCol;
                int chunk = idx / 64;
                int bit = idx % 64;
                
                if (chunk < state.occupied.length && 
                    (state.occupied[chunk] & (1L << bit)) != 0) {
                    break;
                }
                
                moveCount++;
            }
        } 
        else {
            for (int offset = 1; ; offset++) {
                int topmostRow = Distance.findTopmostRow(car, width);
                int checkRow = topmostRow - offset;
                
                if (checkRow < 0) break;
                
                int idx = checkRow * width + car.col;
                int chunk = idx / 64;
                int bit = idx % 64;
                
                if (chunk < state.occupied.length && 
                    (state.occupied[chunk] & (1L << bit)) != 0) {
                    break; // Occupied
                }
                
                moveCount++;
            }
            
            // Check down
            for (int offset = 1; ; offset++) {
                int bottommostRow = Distance.findBottommostRow(car, width);
                int checkRow = bottommostRow + offset;
                
                if (checkRow >= height) break; // Off the board
                
                int idx = checkRow * width + car.col;
                int chunk = idx / 64;
                int bit = idx % 64;
                
                if (chunk < state.occupied.length && 
                    (state.occupied[chunk] & (1L << bit)) != 0) {
                    break; // Occupied
                }
                
                moveCount++;
            }
        }
        
        return moveCount;
    }
}