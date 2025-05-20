package pathfinding;

import java.util.*;

import util.BoardPrinter;
import util.State;

/**
 * Implementation of Uniform Cost Search algorithm for pathfinding.
 */
public class UCS {
    private PriorityQueue<State> queue;
    private Map<String, Integer> costMap; // Maps state hash to lowest cost found
    private int width, height;
    private int kRow, kCol;
    private String exitDirection;
    private int visitedNodeCount;
    
    /**
     * Constructs a Uniform Cost Search solver with specified parameters.
     * 
     * @param width Width of the puzzle grid
     * @param height Height of the puzzle grid
     * @param kRow Row position of the exit
     * @param kCol Column position of the exit
     * @param exitDirection Direction of the exit path
     */
    public UCS(int width, int height, int kRow, int kCol, String exitDirection) {
        this.width = width;
        this.height = height;
        this.kRow = kRow;
        this.kCol = kCol;
        this.exitDirection = exitDirection;
        
        // Use PriorityQueue with custom comparator to order states by cost
        this.queue = new PriorityQueue<>(Comparator.comparingInt(state -> state.cost));
        this.costMap = new HashMap<>();
    }

    /**
     * Finds the optimal path from the initial state to the goal state using UCS.
     * 
     * @param initialState The starting state of the puzzle
     * @return The goal state containing the solution path, or null if no solution exists
     */
    public State find(State initialState) {
        queue.add(initialState);
        costMap.put(getStateHash(initialState), initialState.cost);
        visitedNodeCount = 0;

        while (!queue.isEmpty()) {
            State currentState = queue.poll();
            visitedNodeCount++;
            
            String stateHash = getStateHash(currentState);
            if (costMap.containsKey(stateHash) && costMap.get(stateHash) < currentState.cost) {
            continue;
            }
            
            if (currentState.isReached(width, height, kRow, kCol, exitDirection)) {
            System.out.println("Goal state reached!");
            System.out.println("Visited nodes: " + visitedNodeCount);
            System.out.println("Total cost (steps): " + currentState.cost);
            return currentState;
            }
            
            List<State> successors = currentState.generateNextStates(width, height);
            for (State successor : successors) {
            String successorHash = getStateHash(successor);
            
            if (!costMap.containsKey(successorHash) || successor.cost < costMap.get(successorHash)) {
                costMap.put(successorHash, successor.cost);
                queue.add(successor);
            }
            }
        }
        
        System.out.println("Goal state not reachable.");
        return null;
    }
    
    /**
     * Returns the count of nodes visited during the search.
     * 
     * @return The number of visited nodes
     */
    public int getVisitedNodeCount() {
        return visitedNodeCount;
    }
    
    /**
     * Creates a unique string representation of a state for use in the cost map.
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
            
            for (long mask : state.cars.get(carId).bitmask) {
                sb.append(mask).append(",");
            }
            sb.append(";");
        }
        
        return sb.toString();
    }
}