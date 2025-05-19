package pathfinding;

import java.util.*;
import util.State;
import heuristic.Heuristic;

public class GreedyBFS {
    private PriorityQueue<State> queue;
    private Map<String, Integer> visitedMap; // Maps state hash to heuristic value
    private int width, height;
    private int kRow, kCol;
    private String exitDirection;
    private Heuristic heuristic;
    
    public GreedyBFS(int width, int height, int kRow, int kCol, String exitDirection, Heuristic heuristic) {
        this.width = width;
        this.height = height;
        this.kRow = kRow;
        this.kCol = kCol;
        this.exitDirection = exitDirection;
        this.heuristic = heuristic;
        
        // Priority queue ordered only by heuristic value
        this.queue = new PriorityQueue<>((s1, s2) -> {
            int h1 = calculateHeuristic(s1);
            int h2 = calculateHeuristic(s2);
            return Integer.compare(h1, h2);
        });
        this.visitedMap = new HashMap<>();
    }
    
    // Use the provided heuristic
    private int calculateHeuristic(State state) {
        return heuristic.calculate(state, width, height, exitDirection);
    }
    
    public State find(State initialState) {
        queue.add(initialState);
        visitedMap.put(getStateHash(initialState), calculateHeuristic(initialState));
        int visitedNode = 0;
        
        System.out.println("Using Greedy Best-First Search with heuristic: " + heuristic.getName());
        
        while (!queue.isEmpty()) {
            State currentState = queue.poll();
            visitedNode++;
            
            // Debug output for large searches
            if (visitedNode % 1000 == 0) {
                System.out.println("Visited " + visitedNode + " nodes so far");
            }
            
            if (currentState.isReached(width, height, kRow, kCol, exitDirection)) {
                System.out.println("Goal state reached!");
                System.out.println("Visited nodes: " + visitedNode);
                System.out.println("Total cost (steps): " + currentState.cost);
                return currentState;
            }
            
            List<State> successors = currentState.generateNextStates(width, height);
            for (State successor : successors) {
                String successorHash = getStateHash(successor);
                if (!visitedMap.containsKey(successorHash)) {
                    visitedMap.put(successorHash, calculateHeuristic(successor));
                    queue.add(successor);
                }
            }
        }
        
        System.out.println("Goal state not reachable after exploring " + visitedNode + " nodes.");
        return null;
    }
    
    /**
     * Generate a hash string for a state based on car positions
     * This is used as a key for the visitedMap
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
}