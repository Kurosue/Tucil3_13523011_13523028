package heuristic;

import java.util.*;
import util.Car;
import util.State;

public class MobilityScore implements Heuristic {
    
    @Override
    public String getName() {
        return "Mobility Score";
    }
    
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
        
        // We want high mobility (more available moves) = lower heuristic value
        // But we especially want the primary car to have high mobility
        
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
        
        // The fewer available moves, the higher the heuristic value
        return distanceValue + (expectedMaxCars - (totalAvailableMoves / 2));
    }
    
    private int countPossibleMoves(Car car, State state, int width, int height) {
        int moveCount = 0;
        
        // For horizontal cars, check left and right
        if (car.isHorizontal) {
            // Check left
            for (int offset = 1; ; offset++) {
                int leftmostCol = Distance.findLeftmostColumn(car, width);
                int checkCol = leftmostCol - offset;
                
                if (checkCol < 0) break; // Off the board
                
                int idx = car.row * width + checkCol;
                int chunk = idx / 64;
                int bit = idx % 64;
                
                if (chunk < state.occupied.length && 
                    (state.occupied[chunk] & (1L << bit)) != 0) {
                    break; // Occupied
                }
                
                moveCount++;
            }
            
            // Check right
            for (int offset = 1; ; offset++) {
                int rightmostCol = Distance.findRightmostColumn(car, width);
                int checkCol = rightmostCol + offset;
                
                if (checkCol >= width) break; // Off the board
                
                int idx = car.row * width + checkCol;
                int chunk = idx / 64;
                int bit = idx % 64;
                
                if (chunk < state.occupied.length && 
                    (state.occupied[chunk] & (1L << bit)) != 0) {
                    break; // Occupied
                }
                
                moveCount++;
            }
        } 
        // For vertical cars, check up and down
        else {
            // Check up
            for (int offset = 1; ; offset++) {
                int topmostRow = Distance.findTopmostRow(car, width);
                int checkRow = topmostRow - offset;
                
                if (checkRow < 0) break; // Off the board
                
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