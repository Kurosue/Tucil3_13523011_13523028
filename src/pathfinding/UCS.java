package pathfinding;

import java.util.*;

import util.BoardPrinter;
import util.State;

public class UCS {
    private PriorityQueue<State> queue;
    private Map<String, Integer> costMap; // Maps state hash to lowest cost found
    private int width, height;
    private int kRow, kCol;
    private String exitDirection;
    
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

    public State find(State initialState) {
        queue.add(initialState);
        costMap.put(getStateHash(initialState), initialState.cost);
        int visitedNode = 0;

        while (!queue.isEmpty()) {
            State currentState = queue.poll();
            visitedNode++;
            
            String stateHash = getStateHash(currentState);
            if (costMap.containsKey(stateHash) && costMap.get(stateHash) < currentState.cost) {
            continue;
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
     * Generate a hash string for a state based on car positions
     * This is used as a key for the costMap
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