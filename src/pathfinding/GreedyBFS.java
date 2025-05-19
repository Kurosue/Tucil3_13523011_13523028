package pathfinding;

import java.util.*;
import util.State;
import heuristic.Heuristic;

public class GreedyBFS {
    private PriorityQueue<State> queue;
    private Set<State> visited;
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
        this.visited = new HashSet<>();
    }
    
    // Use the provided heuristic
    private int calculateHeuristic(State state) {
        return heuristic.calculate(state, width, height, exitDirection);
    }
    
    public State find(State initialState) {
        queue.add(initialState);
        visited.add(initialState);
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
                if (!visited.contains(successor)) {
                    visited.add(successor);
                    queue.add(successor);
                }
            }
        }
        
        System.out.println("Goal state not reachable after exploring " + visitedNode + " nodes.");
        return null;
    }
}