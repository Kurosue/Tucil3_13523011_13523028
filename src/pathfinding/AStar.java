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
    private Set<State> visited;
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
        this.visited = new HashSet<>();
    }
    
    private int calculateHeuristic(State state) {
        return heuristic.calculate(state, width, height, exitDirection);
    }
    
    public State find(State initialState) {
        queue.add(initialState);
        Map<State, Integer> bestCost = new HashMap<>();
        bestCost.put(initialState, initialState.cost);
        int visitedNode = 0;
        
        System.out.println("Using A* with heuristic: " + heuristic.getName());
        
        while (!queue.isEmpty()) {
            State currentState = queue.poll();
            visitedNode++;
            
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