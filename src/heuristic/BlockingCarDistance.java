package heuristic;

import java.util.*;
import util.Car;
import util.State;

public class BlockingCarDistance implements Heuristic {
    
    @Override
    public String getName() {
        return "Blocking Car Distance";
    }
    
    @Override
    public int calculate(State state, int width, int height, String exitDirection) {
        Car primaryCar = state.cars.get('P');
        if (primaryCar == null) return Integer.MAX_VALUE;
        
        // Get the blocking cars
        Map<Character, Integer> blockingCarsWithDistance = new HashMap<>();
        
        if (primaryCar.isHorizontal) {
            // For horizontal car, check if exit is horizontal
            if (!("right".equals(exitDirection) || "left".equals(exitDirection))) {
                return Integer.MAX_VALUE; // Incompatible exit direction
            }
            
            // Find car's positions
            int leftmostCol = Distance.findLeftmostColumn(primaryCar, width);
            int rightmostCol = Distance.findRightmostColumn(primaryCar, width);
            int row = primaryCar.row;
            
            if ("right".equals(exitDirection)) {
                // Analyze cars blocking path to the right
                for (int c = rightmostCol + 1; c < width; c++) {
                    int idx = row * width + c;
                    int chunk = idx / 64;
                    int bit = idx % 64;
                    
                    if (chunk < state.occupied.length && (state.occupied[chunk] & (1L << bit)) != 0) {
                        // Found an occupied cell, find which car it is
                        for (Car car : state.cars.values()) {
                            if (car.id == 'P') continue; // Skip primary car
                            
                            if (chunk < car.bitmask.length && (car.bitmask[chunk] & (1L << bit)) != 0) {
                                // Calculate minimum moves needed for this car to clear the path
                                int moveDistance = calculateMinimumMoves(car, row, c, state, width, height);
                                blockingCarsWithDistance.put(car.id, moveDistance);
                            }
                        }
                    }
                }
            } else { // left
                // Analyze cars blocking path to the left
                for (int c = leftmostCol - 1; c >= 0; c--) {
                    int idx = row * width + c;
                    int chunk = idx / 64;
                    int bit = idx % 64;
                    
                    if (chunk < state.occupied.length && (state.occupied[chunk] & (1L << bit)) != 0) {
                        // Find which car it is
                        for (Car car : state.cars.values()) {
                            if (car.id == 'P') continue;
                            
                            if (chunk < car.bitmask.length && (car.bitmask[chunk] & (1L << bit)) != 0) {
                                int moveDistance = calculateMinimumMoves(car, row, c, state, width, height);
                                blockingCarsWithDistance.put(car.id, moveDistance);
                            }
                        }
                    }
                }
            }
        } else { // Vertical car
            // For vertical car, check if exit is vertical
            if (!("top".equals(exitDirection) || "bottom".equals(exitDirection))) {
                return Integer.MAX_VALUE; // Incompatible exit direction
            }
            
            // Find car's positions
            int topmostRow = Distance.findTopmostRow(primaryCar, width);
            int bottommostRow = Distance.findBottommostRow(primaryCar, width);
            int col = primaryCar.col;
            
            if ("bottom".equals(exitDirection)) {
                // Analyze cars blocking path to the bottom
                for (int r = bottommostRow + 1; r < height; r++) {
                    int idx = r * width + col;
                    int chunk = idx / 64;
                    int bit = idx % 64;
                    
                    if (chunk < state.occupied.length && (state.occupied[chunk] & (1L << bit)) != 0) {
                        for (Car car : state.cars.values()) {
                            if (car.id == 'P') continue;
                            
                            if (chunk < car.bitmask.length && (car.bitmask[chunk] & (1L << bit)) != 0) {
                                int moveDistance = calculateMinimumMoves(car, r, col, state, width, height);
                                blockingCarsWithDistance.put(car.id, moveDistance);
                            }
                        }
                    }
                }
            } else { // top
                // Analyze cars blocking path to the top
                for (int r = topmostRow - 1; r >= 0; r--) {
                    int idx = r * width + col;
                    int chunk = idx / 64;
                    int bit = idx % 64;
                    
                    if (chunk < state.occupied.length && (state.occupied[chunk] & (1L << bit)) != 0) {
                        for (Car car : state.cars.values()) {
                            if (car.id == 'P') continue;
                            
                            if (chunk < car.bitmask.length && (car.bitmask[chunk] & (1L << bit)) != 0) {
                                int moveDistance = calculateMinimumMoves(car, r, col, state, width, height);
                                blockingCarsWithDistance.put(car.id, moveDistance);
                            }
                        }
                    }
                }
            }
        }
        
        // Calculate distance to edge
        int distanceToExit;
        if (primaryCar.isHorizontal) {
            int leftmostCol = Distance.findLeftmostColumn(primaryCar, width);
            int rightmostCol = Distance.findRightmostColumn(primaryCar, width);
            
            distanceToExit = "right".equals(exitDirection) ? 
                width - 1 - rightmostCol : leftmostCol;
        } else {
            int topmostRow = Distance.findTopmostRow(primaryCar, width);
            int bottommostRow = Distance.findBottommostRow(primaryCar, width);
            
            distanceToExit = "bottom".equals(exitDirection) ? 
                height - 1 - bottommostRow : topmostRow;
        }
        
        // Sum up the moves needed for all blocking cars
        int totalBlockingMoves = 0;
        for (Integer moves : blockingCarsWithDistance.values()) {
            totalBlockingMoves += moves;
        }
        
        // The heuristic is the sum of distance to exit and moves needed by blocking cars
        return distanceToExit + totalBlockingMoves;
    }
    
    private int calculateMinimumMoves(Car car, int blockingRow, int blockingCol, State state, int width, int height) {
        // For a blocking car, find the minimum moves needed to clear the pathway
        
        if (car.isHorizontal) {
            // For horizontal car, it needs to move left or right to clear the blocking point
            int leftmostCol = Distance.findLeftmostColumn(car, width);
            int rightmostCol = Distance.findRightmostColumn(car, width);
            int length = rightmostCol - leftmostCol + 1;
            
            // Check if moving left or right would be quicker
            int moveLeftDistance = Integer.MAX_VALUE;
            int moveRightDistance = Integer.MAX_VALUE;
            
            // Check moving left
            boolean canMoveLeft = true;
            for (int offset = 1; offset <= leftmostCol; offset++) {
                int checkCol = leftmostCol - offset;
                int idx = car.row * width + checkCol;
                int chunk = idx / 64;
                int bit = idx % 64;
                
                if (chunk < state.occupied.length && 
                    (state.occupied[chunk] & (1L << bit)) != 0) {
                    canMoveLeft = false;
                    break;
                }
                
                // Check if moving left by offset would clear the blocking point
                if (rightmostCol - offset < blockingCol) {
                    moveLeftDistance = offset;
                    break;
                }
            }
            
            // Check moving right
            boolean canMoveRight = true;
            for (int offset = 1; offset < width - rightmostCol; offset++) {
                int checkCol = rightmostCol + offset;
                int idx = car.row * width + checkCol;
                int chunk = idx / 64;
                int bit = idx % 64;
                
                if (chunk < state.occupied.length && 
                    (state.occupied[chunk] & (1L << bit)) != 0) {
                    canMoveRight = false;
                    break;
                }
                
                // Check if moving right by offset would clear the blocking point
                if (leftmostCol + offset > blockingCol) {
                    moveRightDistance = offset;
                    break;
                }
            }
            
            // Return the minimum of the two options, if either is available
            if (canMoveLeft && moveLeftDistance != Integer.MAX_VALUE && 
                canMoveRight && moveRightDistance != Integer.MAX_VALUE) {
                return Math.min(moveLeftDistance, moveRightDistance);
            } else if (canMoveLeft && moveLeftDistance != Integer.MAX_VALUE) {
                return moveLeftDistance;
            } else if (canMoveRight && moveRightDistance != Integer.MAX_VALUE) {
                return moveRightDistance;
            } else {
                return 3; // Couldn't find a clear path, return a penalty value
            }
        } 
        else { // Vertical car
            // For vertical car, it needs to move up or down to clear the blocking point
            int topmostRow = Distance.findTopmostRow(car, width);
            int bottommostRow = Distance.findBottommostRow(car, width);
            int length = bottommostRow - topmostRow + 1;
            
            // Check if moving up or down would be quicker
            int moveUpDistance = Integer.MAX_VALUE;
            int moveDownDistance = Integer.MAX_VALUE;
            
            // Check moving up
            boolean canMoveUp = true;
            for (int offset = 1; offset <= topmostRow; offset++) {
                int checkRow = topmostRow - offset;
                int idx = checkRow * width + car.col;
                int chunk = idx / 64;
                int bit = idx % 64;
                
                if (chunk < state.occupied.length && 
                    (state.occupied[chunk] & (1L << bit)) != 0) {
                    canMoveUp = false;
                    break;
                }
                
                // Check if moving up by offset would clear the blocking point
                if (bottommostRow - offset < blockingRow) {
                    moveUpDistance = offset;
                    break;
                }
            }
            
            // Check moving down
            boolean canMoveDown = true;
            for (int offset = 1; offset < height - bottommostRow; offset++) {
                int checkRow = bottommostRow + offset;
                int idx = checkRow * width + car.col;
                int chunk = idx / 64;
                int bit = idx % 64;
                
                if (chunk < state.occupied.length && 
                    (state.occupied[chunk] & (1L << bit)) != 0) {
                    canMoveDown = false;
                    break;
                }
                
                // Check if moving down by offset would clear the blocking point
                if (topmostRow + offset > blockingRow) {
                    moveDownDistance = offset;
                    break;
                }
            }
            
            // Return the minimum of the two options, if either is available
            if (canMoveUp && moveUpDistance != Integer.MAX_VALUE && 
                canMoveDown && moveDownDistance != Integer.MAX_VALUE) {
                return Math.min(moveUpDistance, moveDownDistance);
            } else if (canMoveUp && moveUpDistance != Integer.MAX_VALUE) {
                return moveUpDistance;
            } else if (canMoveDown && moveDownDistance != Integer.MAX_VALUE) {
                return moveDownDistance;
            } else {
                return 3; // Couldn't find a clear path
            }
        }
    }
}