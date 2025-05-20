package heuristic;

import util.State;

/* 
Heuristikku, sang intuisi,
Perkirakan jalan dengan presisi,
Seakan bintang membisik lembut,
Jalur mana yang patut aku rebut.
*/

public interface Heuristic {
    /**
     * Calculates the heuristic value for a given state.
     * 
     * @param state The state to evaluate
     * @param width Width of the puzzle grid
     * @param height Height of the puzzle grid
     * @param exitDirection Direction of the exit path
     * @return An estimate of the cost from the current state to the goal
     */
    int calculate(State state, int width, int height, String exitDirection);
    
    /**
     * Returns the name of the heuristic.
     * 
     * @return A descriptive name of the heuristic function
     */
    String getName();
}