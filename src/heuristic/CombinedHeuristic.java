package heuristic;

import util.State;

/**
 * Combined heuristic that considers both distance to exit and blocking cars.
 */
public class CombinedHeuristic implements Heuristic {
    
    /**
     * Returns the name of this heuristic function.
     * 
     * @return String name of the heuristic
     */
    @Override
    public String getName() {
        return "Distance + Blocking Cars";
    }
    
    /**
     * Calculates a combined heuristic value using both Distance and BlockingCars.
     * Adds the distance to exit and twice the number of blocking cars.
     * 
     * @param state The current puzzle state
     * @param width Width of the puzzle grid
     * @param height Height of the puzzle grid
     * @param exitDirection Direction of the exit ("left", "right", "top", "bottom")
     * @return Combined heuristic value or MAX_VALUE if state is invalid
     */
    @Override
    public int calculate(State state, int width, int height, String exitDirection) {
        Distance distance = new Distance();
        BlockingCars blockingCars = new BlockingCars();
        
        int distValue = distance.calculate(state, width, height, exitDirection);
        int blockingValue = blockingCars.calculate(state, width, height, exitDirection);
        
        // If either heuristic returns MAX_VALUE, the state is invalid
        if (distValue == Integer.MAX_VALUE || blockingValue == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        
        // Combine both heuristics - distance plus 2x the number of blocking cars
        // The multiplier can be adjusted for different behavior
        return distValue + (2 * blockingValue);
    }
}