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
    public long[] occupied;
    public int cost;

    public State(Map<Character, Car> cars, State parent, String move, int cost) {
        this.cars = cars;
        this.parent = parent;
        this.move = move;
        this.cost = cost;
        
        int totalBits = 64;
        for (Car car : cars.values()) {
            totalBits = Math.max(totalBits, car.bitmask.length * 64);
        }
        
        int chunkCount = (totalBits + 63) / 64;
        this.occupied = new long[chunkCount];
        
        for (Car car : cars.values()) {
            for (int i = 0; i < car.bitmask.length && i < chunkCount; i++) {
                this.occupied[i] |= car.bitmask[i];
            }
        }
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
            for (int i = 0; i < chunkCount && i < car.bitmask.length; i++) {
                occupied[i] |= car.bitmask[i];
            }
        }

        return occupied;
    }

    public boolean isReached(int width, int height) {
        Car primaryCar = this.cars.get('P');
        if (primaryCar == null) return false;

        if (primaryCar.isHorizontal) {
            int row = primaryCar.row;
            
            long[] rowMask = new long[occupied.length];
            for (int c = 0; c < width; c++) {
                int idx = row * width + c;
                int chunk = idx / 64;
                int bit = idx % 64;
                
                if (chunk < rowMask.length) {
                    rowMask[chunk] |= (1L << bit);
                }
            }
            
            for (int i = 0; i < occupied.length; i++) {
                long occupiedInRow = occupied[i] & rowMask[i];
                long primaryCarBits = primaryCar.bitmask[i] & rowMask[i];
                
                if ((occupiedInRow & ~primaryCarBits) != 0) {
                    return false;
                }
            }
            
            return true;
        } 
        else {
            int col = primaryCar.col;
            
            long[] colMask = new long[occupied.length];
            for (int r = 0; r < height; r++) {
                int idx = r * width + col;
                int chunk = idx / 64;
                int bit = idx % 64;
                
                if (chunk < colMask.length) {
                    colMask[chunk] |= (1L << bit);
                }
            }
            
            for (int i = 0; i < occupied.length; i++) {
                long occupiedInCol = occupied[i] & colMask[i];
                long primaryCarBits = primaryCar.bitmask[i] & colMask[i];
                
                if ((occupiedInCol & ~primaryCarBits) != 0) {
                    return false;
                }
            }
            
            return true;
        }
    }

    public List<State> generateNextStates(int width, int height) {
        List<State> nextStates = new ArrayList<>();
        
        for (Map.Entry<Character, Car> entry : cars.entrySet()) {
            char carId = entry.getKey();
            Car car = entry.getValue();
            
            for (Direction dir : car.getPossibleDirections()) {
                String moveDesc = carId + "-" + dir.name().toLowerCase();
                
                Car movedCar = car.shift(dir, width, height);
                
                while (movedCar != null) {
                    boolean collision = false;
                    
                    Map<Character, Car> tempCars = new HashMap<>(cars);
                    tempCars.put(carId, movedCar);
                    long[] tempOccupied = buildOccupiedMask(tempCars, width, height);
                    
                    int totalBits = 0;
                    for (Car c : tempCars.values()) {
                        for (int i = 0; i < c.bitmask.length; i++) {
                            totalBits += Long.bitCount(c.bitmask[i]);
                        }
                    }
                    
                    int occupiedBits = 0;
                    for (int i = 0; i < tempOccupied.length; i++) {
                        occupiedBits += Long.bitCount(tempOccupied[i]);
                    }
                    
                    if (totalBits != occupiedBits) {
                        collision = true;
                    }
                    
                    if (!collision) {
                        State nextState = this.copy(this, moveDesc);
                        nextState.cars.put(carId, movedCar);
                        nextState.occupied = tempOccupied;
                        nextStates.add(nextState);
                        
                        movedCar = movedCar.shift(dir, width, height);
                    } else {
                        break;
                    }
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

    @Override
    public int hashCode() {
        return cars.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof State other)) return false;
        return this.cars.equals(other.cars);
    }

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