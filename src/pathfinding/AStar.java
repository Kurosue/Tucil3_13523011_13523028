package pathfinding;

import java.util.*;
import util.State;
import util.Car;

public class AStar {
    private PriorityQueue<State> queue;
    private Set<State> visited;
    private long[] goalMask;
    private int width, height;
    private int kRow, kCol;
    private String exitDirection;
    
    public AStar(long[] goalMask, int width, int height, int kRow, int kCol, String exitDirection) {
        this.goalMask = goalMask;
        this.width = width;
        this.height = height;
        this.kRow = kRow;
        this.kCol = kCol;
        this.exitDirection = exitDirection;
        // Priority queue with f(n) = g(n) + h(n) comparator
        this.queue = new PriorityQueue<>((s1, s2) -> {
            int f1 = s1.cost + calculateHeuristic(s1);
            int f2 = s2.cost + calculateHeuristic(s2);
            return Integer.compare(f1, f2);
        });
        this.visited = new HashSet<>();
    }
    
    // Heuristic: minimum distance to any valid exit
    private int calculateHeuristic(State state) {
        Car primaryCar = state.cars.get('P');
        if (primaryCar == null) return 0; // Safety check
        
        // For horizontal primary car: find min distance to either left or right edge
        if (primaryCar.isHorizontal) {
            // Get leftmost and rightmost positions
            int leftmostCol = findLeftmostColumn(primaryCar);
            int rightmostCol = findRightmostColumn(primaryCar);
            
            // Calculate distances to both edges
            int distToRight = width - 1 - rightmostCol;
            int distToLeft = leftmostCol;
            
            // Return minimum distance to either edge
            return Math.min(distToRight, distToLeft);
        } 
        // For vertical primary car: find min distance to either top or bottom edge
        else {
            // Get topmost and bottommost positions
            int topmostRow = findTopmostRow(primaryCar);
            int bottommostRow = findBottommostRow(primaryCar);
            
            // Calculate distances to both edges
            int distToBottom = height - 1 - bottommostRow;
            int distToTop = topmostRow;
            
            // Return minimum distance to either edge
            return Math.min(distToBottom, distToTop);
        }
    }
    
    // Find the leftmost column occupied by a car
    private int findLeftmostColumn(Car car) {
        int leftmost = Integer.MAX_VALUE;
        for (int chunk = 0; chunk < car.bitmask.length; chunk++) {
            long bits = car.bitmask[chunk];
            if (bits == 0) continue; // Skip if no bits set in this chunk
            
            for (int bit = 0; bit < 64; bit++) {
                if ((bits & (1L << bit)) != 0) {
                    int index = chunk * 64 + bit;
                    int col = index % width;
                    leftmost = Math.min(leftmost, col);
                }
            }
        }
        return leftmost;
    }
    
    // Find the rightmost column occupied by a car
    private int findRightmostColumn(Car car) {
        int rightmost = -1;
        for (int chunk = 0; chunk < car.bitmask.length; chunk++) {
            long bits = car.bitmask[chunk];
            if (bits == 0) continue; // Skip if no bits set in this chunk
            
            for (int bit = 0; bit < 64; bit++) {
                if ((bits & (1L << bit)) != 0) {
                    int index = chunk * 64 + bit;
                    int col = index % width;
                    rightmost = Math.max(rightmost, col);
                }
            }
        }
        return rightmost;
    }
    
    // Find the topmost row occupied by a car
    private int findTopmostRow(Car car) {
        int topmost = Integer.MAX_VALUE;
        for (int chunk = 0; chunk < car.bitmask.length; chunk++) {
            long bits = car.bitmask[chunk];
            if (bits == 0) continue; // Skip if no bits set in this chunk
            
            for (int bit = 0; bit < 64; bit++) {
                if ((bits & (1L << bit)) != 0) {
                    int index = chunk * 64 + bit;
                    int row = index / width;
                    topmost = Math.min(topmost, row);
                }
            }
        }
        return topmost;
    }
    
    // Find the bottommost row occupied by a car
    private int findBottommostRow(Car car) {
        int bottommost = -1;
        for (int chunk = 0; chunk < car.bitmask.length; chunk++) {
            long bits = car.bitmask[chunk];
            if (bits == 0) continue; // Skip if no bits set in this chunk
            
            for (int bit = 0; bit < 64; bit++) {
                if ((bits & (1L << bit)) != 0) {
                    int index = chunk * 64 + bit;
                    int row = index / width;
                    bottommost = Math.max(bottommost, row);
                }
            }
        }
        return bottommost;
    }
    
    public State find(State initialState) {
        queue.add(initialState);
        Map<State, Integer> bestCost = new HashMap<>();
        bestCost.put(initialState, initialState.cost);
        int visitedNode = 0;
        
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
                Integer previousCost = bestCost.get(successor);
                if (previousCost == null || successor.cost < previousCost) {
                    bestCost.put(successor, successor.cost);
                    queue.add(successor);
                }
            }
        }
        
        System.out.println("Goal state not reachable after exploring " + visitedNode + " nodes.");
        return null;
    }
}