package heuristic;

import util.State;

public interface Heuristic {
    int calculate(State state, int width, int height, String exitDirection);
    String getName();
}