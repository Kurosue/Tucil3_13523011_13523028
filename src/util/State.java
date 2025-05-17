package util;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import util.BitBoard;
import util.Car;

public class State {
    public Map<Character, Car> cars;
    public BitBoard occupied;
    public String moveHistory;
    public int cost;

    public State(Map<Character, Car> cars, BitBoard occupied, String moveHistory, int cost) {
        this.cars = cars;
        this.occupied = occupied;
        this.moveHistory = moveHistory;
        this.cost = cost;
    }

    public State copy() {
        Map<Character, Car> carCopy = new HashMap<>();
        for (var entry : cars.entrySet()) {
            carCopy.put(entry.getKey(), entry.getValue().copy());
        }
        return new State(carCopy, new BitBoard(occupied), moveHistory, cost);
    }

    // Untuk hashing dan pengecekan visited
    @Override
    public int hashCode() {
        return Arrays.hashCode(occupied.getBits());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof State other)) return false;
        return Arrays.equals(this.occupied.getBits(), other.occupied.getBits());
    }
}
