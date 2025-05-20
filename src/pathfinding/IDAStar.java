package pathfinding;

import java.util.*;
import util.State;
import heuristic.Heuristic;

/**
 * Implementation of Iterative Deepening A* search algorithm.
 */
public class IDAStar {
    private int width, height;
    private int kRow, kCol;
    private String exitDirection;
    private Heuristic heuristic;
    private int visitedNode;
    
    /**
     * Constructs an IDA* search solver with specified parameters.
     * 
     * @param width Width of the puzzle grid
     * @param height Height of the puzzle grid
     * @param kRow Row position of the exit
     * @param kCol Column position of the exit
     * @param exitDirection Direction of the exit path
     * @param heuristic Heuristic function to use for evaluation
     */
    public IDAStar(int width, int height, int kRow, int kCol, String exitDirection, Heuristic heuristic) {
        this.width = width;
        this.height = height;
        this.kRow = kRow;
        this.kCol = kCol;
        this.exitDirection = exitDirection;
        this.heuristic = heuristic;
    }
    
    /**
     * Finds a path from the initial state to the goal state using IDA* search.
     * 
     * @param initialState The starting state of the puzzle
     * @return The goal state containing the solution path, or null if no solution exists
     */
    public State find(State initialState) {
        visitedNode = 0;
        int threshold = calculateHeuristic(initialState);
        
        System.out.println("Using IDA* with heuristic: " + heuristic.getName());
        
        while (threshold < Integer.MAX_VALUE) {
            System.out.println("Current threshold: " + threshold);
            int nextThreshold = Integer.MAX_VALUE;
            Set<String> visitedStates = new HashSet<>(); // Using string hashes
            SearchResult result = search(initialState, 0, threshold, visitedStates, nextThreshold);
            
            if (result.state != null) {
                System.out.println("Goal state reached!");
                System.out.println("Visited nodes: " + visitedNode);
                System.out.println("Total cost (steps): " + result.state.cost);
                return result.state;
            }
            
            threshold = result.nextThreshold;
            if (threshold == Integer.MAX_VALUE) {
                System.out.println("Goal state not reachable after exploring " + visitedNode + " nodes.");
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * Recursive depth-first search with iterative deepening based on f-value threshold.
     * 
     * @param state Current state being explored
     * @param g Current path cost
     * @param threshold Current f-value threshold
     * @param visitedStates Set of visited states in current path
     * @param nextThreshold Minimum f-value exceeding current threshold
     * @return Search result containing found state or next threshold value
     */
    private SearchResult search(State state, int g, int threshold, Set<String> visitedStates, int nextThreshold) {
        visitedNode++;
        
        int f = g + calculateHeuristic(state);
        if (f > threshold) {
            return new SearchResult(null, f);
        }
        
        if (state.isReached(width, height, kRow, kCol, exitDirection)) {
            return new SearchResult(state, threshold);
        }
        
        String stateHash = getStateHash(state);
        visitedStates.add(stateHash);
        int min = Integer.MAX_VALUE;
        
        List<State> successors = state.generateNextStates(width, height);
        for (State successor : successors) {
            String successorHash = getStateHash(successor);
            if (!visitedStates.contains(successorHash)) {
                SearchResult result = search(successor, successor.cost, threshold, visitedStates, nextThreshold);
                if (result.state != null) {
                    return result;
                }
                min = Math.min(min, result.nextThreshold);
            }
        }
        
        visitedStates.remove(stateHash);
        return new SearchResult(null, min);
    }
    
    /**
     * Calculates the heuristic value for the given state.
     * 
     * @param state The state to evaluate
     * @return The heuristic value representing estimated cost to goal
     */
    private int calculateHeuristic(State state) {
        return heuristic.calculate(state, width, height, exitDirection);
    }
    
    /**
     * Creates a unique string representation of a state for tracking visited states.
     * 
     * @param state The state to convert to a hash string
     * @return A string uniquely identifying the state configuration
     */
    private String getStateHash(State state) {
        // A more efficient and reliable way of hashing the state 
        // than relying on the default hashCode
        StringBuilder sb = new StringBuilder();
        
        // Sort car IDs for consistent ordering
        List<Character> carIds = new ArrayList<>(state.cars.keySet());
        Collections.sort(carIds);
        
        for (char carId : carIds) {
            sb.append(carId).append(":");
            
            // Append bitmask representation for each car
            for (long mask : state.cars.get(carId).bitmask) {
                sb.append(mask).append(",");
            }
            sb.append(";");
        }
        
        return sb.toString();
    }
    
    /**
     * Returns the count of nodes visited during the search.
     * 
     * @return The number of visited nodes
     */
    public int getVisitedNodeCount() {
        return visitedNode;
    }
    
    /**
     * Container class for search results in IDA*.
     */
    private class SearchResult {
        State state;
        int nextThreshold;
        
        /**
         * Creates a search result with found state or next threshold.
         * 
         * @param state Found goal state or null if not found
         * @param nextThreshold Next f-value threshold to try
         */
        SearchResult(State state, int nextThreshold) {
            this.state = state;
            this.nextThreshold = nextThreshold;
        }
    }
}