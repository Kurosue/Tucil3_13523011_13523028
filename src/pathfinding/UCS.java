package pathfinding;

import java.util.*;

import util.BoardPrinter;
import util.State;

public class UCS {
    private Queue<State> queue;
    private Set<State> visited;
    private int width, height;
    private int kRow, kCol;
    private String exitDirection;

    public UCS(int width, int height, int kRow, int kCol, String exitDirection) {
        this.width = width;
        this.height = height;
        this.kRow = kRow;
        this.kCol = kCol;
        this.exitDirection = exitDirection;
        this.queue = new LinkedList<>();
        this.visited = new HashSet<>();
    }

    public State find(State initialState) {
        queue.add(initialState);
        visited.add(initialState);
        int visitedNode = 0;
        BoardPrinter.printBoard(initialState, width, height);

        while (!queue.isEmpty()) {
            State currentState = queue.poll();
            visitedNode++;
            
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

        System.out.println("Goal state not reachable.");
        return null;
    }
}