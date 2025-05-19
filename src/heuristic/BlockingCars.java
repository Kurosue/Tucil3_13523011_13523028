package heuristic;

import java.util.HashSet;
import java.util.Set;
import util.Car;
import util.State;

public class BlockingCars implements Heuristic {
    
    @Override
    public String getName() {
        return "Number of Blocking Cars";
    }
    
    @Override
    public int calculate(State state, int width, int height, String exitDirection) {
        Car primaryCar = state.cars.get('P');
        if (primaryCar == null) return Integer.MAX_VALUE;
        
        // Count how many cars are blocking the path to the exit
        Set<Character> blockingCars = new HashSet<>();
        
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
                // Check for cars blocking path to the right
                for (int c = rightmostCol + 1; c < width; c++) {
                    int idx = row * width + c;
                    int chunk = idx / 64;
                    int bit = idx % 64;
                    
                    if (chunk < state.occupied.length && (state.occupied[chunk] & (1L << bit)) != 0) {
                        // Found an occupied cell, find which car it is
                        for (Car car : state.cars.values()) {
                            if (car.id == 'P') continue; // Skip primary car
                            
                            if (chunk < car.bitmask.length && (car.bitmask[chunk] & (1L << bit)) != 0) {
                                blockingCars.add(car.id);
                            }
                        }
                    }
                }
            } else { // left
                // Check for cars blocking path to the left
                for (int c = 0; c < leftmostCol; c++) {
                    int idx = row * width + c;
                    int chunk = idx / 64;
                    int bit = idx % 64;
                    
                    if (chunk < state.occupied.length && (state.occupied[chunk] & (1L << bit)) != 0) {
                        // Found an occupied cell, find which car it is
                        for (Car car : state.cars.values()) {
                            if (car.id == 'P') continue; // Skip primary car
                            
                            if (chunk < car.bitmask.length && (car.bitmask[chunk] & (1L << bit)) != 0) {
                                blockingCars.add(car.id);
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
                // Check for cars blocking path to the bottom
                for (int r = bottommostRow + 1; r < height; r++) {
                    int idx = r * width + col;
                    int chunk = idx / 64;
                    int bit = idx % 64;
                    
                    if (chunk < state.occupied.length && (state.occupied[chunk] & (1L << bit)) != 0) {
                        // Found an occupied cell, find which car it is
                        for (Car car : state.cars.values()) {
                            if (car.id == 'P') continue; // Skip primary car
                            
                            if (chunk < car.bitmask.length && (car.bitmask[chunk] & (1L << bit)) != 0) {
                                blockingCars.add(car.id);
                            }
                        }
                    }
                }
            } else { // top
                // Check for cars blocking path to the top
                for (int r = 0; r < topmostRow; r++) {
                    int idx = r * width + col;
                    int chunk = idx / 64;
                    int bit = idx % 64;
                    
                    if (chunk < state.occupied.length && (state.occupied[chunk] & (1L << bit)) != 0) {
                        // Found an occupied cell, find which car it is
                        for (Car car : state.cars.values()) {
                            if (car.id == 'P') continue; // Skip primary car
                            
                            if (chunk < car.bitmask.length && (car.bitmask[chunk] & (1L << bit)) != 0) {
                                blockingCars.add(car.id);
                            }
                        }
                    }
                }
            }
        }
        
        return blockingCars.size();
    }
}