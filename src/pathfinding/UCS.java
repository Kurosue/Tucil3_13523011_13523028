package pathfinding;

import java.util.*;
import util.State;

public class UCS {
    private Queue<State> queue; // Uniform cost → queue biasa cukup
    private Set<State> visited;
    private long[] goalMask;
    private int width, height;

    public UCS(long[] goalMask, int width, int height) {
        this.goalMask = goalMask;
        this.width = width;
        this.height = height;
        this.queue = new LinkedList<>();
        this.visited = new HashSet<>();
    }

    public State find(State initialState) {
        queue.add(initialState);
        visited.add(initialState);
        int visitedNode = 0;

        while (!queue.isEmpty()) {
            State currentState = queue.poll();
            visitedNode++;

            if (currentState.isGoalReached(width, height)) {
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
