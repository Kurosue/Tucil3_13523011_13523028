package heuristic;

import util.State;

public class CombinedHeuristic implements Heuristic {
    
    @Override
    public String getName() {
        return "Distance + Blocking Cars";
    }
    
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