package util;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import util.Car.Direction;

public class State {
    public Map<Character, Car> cars;
    public State parent;
    public String move;
    public int cost;

    public State(Map<Character, Car> cars, State parent, String move, int cost) {
        this.cars = cars;
        this.parent = parent;
        this.move = move;
        this.cost = cost;
    }

    public State copy(State newParent, String newMove) {
        Map<Character, Car> newCars = new HashMap<>();
        for (Map.Entry<Character, Car> entry : cars.entrySet()) {
            newCars.put(entry.getKey(), entry.getValue().copy());
        }
        return new State(newCars, newParent, newMove, cost + 1);
    }

    public static long[] buildOccupiedMask(Map<Character, Car> cars, int width, int height) {
        int totalBits = width * height;
        int chunkCount = (totalBits + 63) / 64;
        long[] occupied = new long[chunkCount];

        for (Car car : cars.values()) {
            for (int i = 0; i < chunkCount; i++) {
                occupied[i] |= car.bitmask[i];
            }
        }

        return occupied;
    }

    public boolean isGoalReached(int width, int height) {
        Car p = this.cars.get('P');

        // Cek setiap posisi bit mobil P
        for (int chunk = 0; chunk < p.bitmask.length; chunk++) {
            long bits = p.bitmask[chunk];
            for (int bit = 0; bit < 64; bit++) {
                if ((bits & (1L << bit)) != 0) {
                    int index = chunk * 64 + bit;
                    int row = index / width;
                    int col = index % width;

                    // Cek jika mobil horizontal: col == width - 1
                    if (p.isHorizontal && (col == 0 || col == width - 1)) return true;

                    // Cek jika mobil vertikal: row == height - 1
                    if (!p.isHorizontal && (row == 0 || row == height - 1)) return true;
                }
            }
        }

        return false;
    }


    public List<State> generateNextStates(int width, int height) {
        List<State> nextStates = new ArrayList<>();
        long[] occupied = buildOccupiedMask(cars, width, height);

        for (Map.Entry<Character, Car> entry : cars.entrySet()) {
            Car car = entry.getValue();
            for (Direction dir : car.getPossibleDirections()) {
                Car moved = car.shift(dir, width, height);
                while (moved != null && !collides(moved, occupied, car.id)) {
                    String moveStr = car.id + "-" + dir.name().toLowerCase();
                    State next = this.copy(this, moveStr);
                    next.cars.put(car.id, moved);
                    nextStates.add(next);

                    moved = moved.shift(dir, width, height);
                }
            }
        }

        return nextStates;
    }

    private boolean collides(Car moved, long[] occupied, char ignoreId) {
        for (int i = 0; i < occupied.length; i++) {
            long mask = occupied[i];
            if ((mask & moved.bitmask[i]) != 0) {
                Car original = cars.get(ignoreId);
                if ((original.bitmask[i] & moved.bitmask[i]) != 0) {
                    long overlap = original.bitmask[i] & moved.bitmask[i];
                    if ((mask ^ overlap & moved.bitmask[i]) != 0) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    // For visited checking
    @Override
    public int hashCode() {
        return cars.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof State other)) return false;
        return this.cars.equals(other.cars);
    }

    // Helper to reconstruct move history
    public List<String> getMoveHistory() {
        List<String> moves = new ArrayList<>();
        State cur = this;
        while (cur != null && cur.move != null) {
            moves.add(cur.move);
            cur = cur.parent;
        }
        Collections.reverse(moves);
        return moves;
    }
}
