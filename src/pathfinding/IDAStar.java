package pathfinding;

import java.util.*;
import util.State;
import heuristic.Heuristic;

public class IDAStar {
    private int width, height;
    private int kRow, kCol;
    private String exitDirection;
    private Heuristic heuristic;
    private int visitedNode;
    
    public IDAStar(int width, int height, int kRow, int kCol, String exitDirection, Heuristic heuristic) {
        this.width = width;
        this.height = height;
        this.kRow = kRow;
        this.kCol = kCol;
        this.exitDirection = exitDirection;
        this.heuristic = heuristic;
    }
    
    public State find(State initialState) {
        visitedNode = 0;
        int threshold = calculateHeuristic(initialState);
        
        System.out.println("Using IDA* with heuristic: " + heuristic.getName());
        
        while (threshold < Integer.MAX_VALUE) {
            System.out.println("Current threshold: " + threshold);
            int nextThreshold = Integer.MAX_VALUE;
            SearchResult result = search(initialState, 0, threshold, new HashSet<>(), nextThreshold);
            
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
    
    private SearchResult search(State state, int g, int threshold, Set<State> visited, int nextThreshold) {
        visitedNode++;
        
        int f = g + calculateHeuristic(state);
        if (f > threshold) {
            return new SearchResult(null, f);
        }
        
        if (state.isReached(width, height, kRow, kCol, exitDirection)) {
            return new SearchResult(state, threshold);
        }
        
        visited.add(state);
        int min = Integer.MAX_VALUE;
        
        List<State> successors = state.generateNextStates(width, height);
        for (State successor : successors) {
            if (!visited.contains(successor)) {
                SearchResult result = search(successor, successor.cost, threshold, visited, nextThreshold);
                if (result.state != null) {
                    return result;
                }
                min = Math.min(min, result.nextThreshold);
            }
        }
        
        visited.remove(state);
        return new SearchResult(null, min);
    }
    
    private int calculateHeuristic(State state) {
        return heuristic.calculate(state, width, height, exitDirection);
    }
    
    private class SearchResult {
        State state;
        int nextThreshold;
        
        SearchResult(State state, int nextThreshold) {
            this.state = state;
            this.nextThreshold = nextThreshold;
        }
    }
}