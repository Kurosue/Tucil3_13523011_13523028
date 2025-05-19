package pathfinding;

import java.util.*;
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
    private Map<String, Integer> costMap; // Maps state hash to best cost found
    private int width, height;
    private int kRow, kCol;
    private String exitDirection;
    private Heuristic heuristic;
    
    public AStar(int width, int height, int kRow, int kCol, String exitDirection, Heuristic heuristic) {
        this.width = width;
        this.height = height;
        this.kRow = kRow;
        this.kCol = kCol;
        this.exitDirection = exitDirection;
        this.heuristic = heuristic;
        
        this.queue = new PriorityQueue<>((s1, s2) -> {
            int f1 = s1.cost + calculateHeuristic(s1);
            int f2 = s2.cost + calculateHeuristic(s2);
            return Integer.compare(f1, f2);
        });
        this.costMap = new HashMap<>();
    }
    
    private int calculateHeuristic(State state) {
        return heuristic.calculate(state, width, height, exitDirection);
    }
    
    public State find(State initialState) {
        queue.add(initialState);
        costMap.put(getStateHash(initialState), initialState.cost);
        int visitedNode = 0;
        
        System.out.println("Using A* with heuristic: " + heuristic.getName());
        
        while (!queue.isEmpty()) {
            State currentState = queue.poll();
            visitedNode++;
            
            String stateHash = getStateHash(currentState);
            if (costMap.containsKey(stateHash) && costMap.get(stateHash) < currentState.cost) {
                continue; // Skip this state if a better path was already found
            }
            
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
                if (!costMap.containsKey(successorHash) || successor.cost < costMap.get(successorHash)) {
                    costMap.put(successorHash, successor.cost);
                    queue.add(successor);
                }
            }
        }
        
        System.out.println("Goal state not reachable after exploring " + visitedNode + " nodes.");
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