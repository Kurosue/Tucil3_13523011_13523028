package pathfinding;

import java.util.*;

import util.BoardPrinter;
import util.State;
import heuristic.Heuristic;
import heuristic.Distance;

/* 
Dalam malam bertabur bintang,
Langkah awal terpampang,
Menapaki simpul-simpul harapan,
Menuju terang dalam kegelapan.
*/

public class AStar {
    private PriorityQueue<State> queue;
    private Map<String, Integer> costMap; 
    private int width, height;
    private int kRow, kCol;
    private String exitDirection;
    private Heuristic heuristic;
    private int visitedNodeCount; // Add field to store visited node count
    
    /**
     * Constructs an A* search solver with specified parameters.
     * 
     * @param width Width of the puzzle grid
     * @param height Height of the puzzle grid
     * @param kRow Row position of the exit
     * @param kCol Column position of the exit
     * @param exitDirection Direction of the exit path
     * @param heuristic Heuristic function to use for evaluation
     */
    public AStar(int width, int height, int kRow, int kCol, String exitDirection, Heuristic heuristic) {
        this.width = width;
        this.height = height;
        this.kRow = kRow;
        this.kCol = kCol;
        this.exitDirection = exitDirection;
        this.heuristic = heuristic;
        this.visitedNodeCount = 0; // Initialize counter
        
        this.queue = new PriorityQueue<>((s1, s2) -> {
            int f1 = s1.cost + calculateHeuristic(s1);
            int f2 = s2.cost + calculateHeuristic(s2);
            return Integer.compare(f1, f2);
        });
        this.costMap = new HashMap<>();
    }
    
    /**
     * Returns the number of nodes visited during the search.
     * 
     * @return The count of visited nodes
     */
    public int getVisitedNodeCount() {
        return visitedNodeCount;
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
     * Finds a path from the initial state to the goal state using A* search.
     * 
     * @param initialState The starting state of the puzzle
     * @return The goal state containing the solution path, or null if no solution exists
     */
    public State find(State initialState) {
        queue.add(initialState);
        costMap.put(getStateHash(initialState), initialState.cost);
        visitedNodeCount = 0; // Reset counter
        
        System.out.println("Using A* with heuristic: " + heuristic.getName());
        
        while (!queue.isEmpty()) {
            State currentState = queue.poll();
            visitedNodeCount++; // Increment counter when visiting a node
            
            String stateHash = getStateHash(currentState);
            if (costMap.containsKey(stateHash) && costMap.get(stateHash) < currentState.cost) {
                continue;
            }
            
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
                if (!costMap.containsKey(successorHash) || successor.cost < costMap.get(successorHash)) {
                    costMap.put(successorHash, successor.cost);
                    queue.add(successor);
                }
            }
        }
        
        System.out.println("Goal state not reachable after exploring " + visitedNodeCount + " nodes.");
        return null;
    }
    
    /**
     * Generate a hash string for a state based on car positions
     * This is used as a key for the costMap
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