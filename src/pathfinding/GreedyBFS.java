package pathfinding;

import java.util.*;
import util.State;
import heuristic.Heuristic;

/**
 * Implementation of Greedy Best-First Search algorithm for pathfinding.
 */
public class GreedyBFS {
    private PriorityQueue<State> queue;
    private Map<String, Integer> visitedMap;
    private int width, height;
    private int kRow, kCol;
    private String exitDirection;
    private Heuristic heuristic;
    private int visitedNodeCount;
    
    /**
     * Constructs a Greedy Best-First Search solver with specified parameters.
     * 
     * @param width Width of the puzzle grid
     * @param height Height of the puzzle grid
     * @param kRow Row position of the target car
     * @param kCol Column position of the target car
     * @param exitDirection Direction of the exit path
     * @param heuristic Heuristic function to use for evaluation
     */
    public GreedyBFS(int width, int height, int kRow, int kCol, String exitDirection, Heuristic heuristic) {
        this.width = width;
        this.height = height;
        this.kRow = kRow;
        this.kCol = kCol;
        this.exitDirection = exitDirection;
        this.heuristic = heuristic;
        
        this.queue = new PriorityQueue<>((s1, s2) -> {
            int h1 = calculateHeuristic(s1);
            int h2 = calculateHeuristic(s2);
            return Integer.compare(h1, h2);
        });
        this.visitedMap = new HashMap<>();
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
     * Finds a path from the initial state to the goal state using Greedy Best-First Search.
     * 
     * @param initialState The starting state of the puzzle
     * @return The goal state containing the solution path, or null if no solution exists
     */
    public State find(State initialState) {
        queue.add(initialState);
        visitedMap.put(getStateHash(initialState), calculateHeuristic(initialState));
        visitedNodeCount = 0;
        
        System.out.println("Using Greedy Best-First Search with heuristic: " + heuristic.getName());
        
        while (!queue.isEmpty()) {
            State currentState = queue.poll();
            visitedNodeCount++;
            
            if (visitedNodeCount % 1000 == 0) {
                System.out.println("Visited " + visitedNodeCount + " nodes so far");
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
                if (!visitedMap.containsKey(successorHash)) {
                    visitedMap.put(successorHash, calculateHeuristic(successor));
                    queue.add(successor);
                }
            }
        }
        
        System.out.println("Goal state not reachable after exploring " + visitedNodeCount + " nodes.");
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
     * Creates a unique string representation of a state for use in the visited map.
     * 
     * @param state The state to convert to a hash string
     * @return A string uniquely identifying the state configuration
     */
    private String getStateHash(State state) {
        StringBuilder sb = new StringBuilder();
        
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