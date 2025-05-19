package heuristic;

import util.State;

/* 
Heuristikku, sang intuisi,
Perkirakan jalan dengan presisi,
Seakan bintang membisik lembut,
Jalur mana yang patut aku rebut.
*/

public interface Heuristic {
    int calculate(State state, int width, int height, String exitDirection);
    String getName();
}