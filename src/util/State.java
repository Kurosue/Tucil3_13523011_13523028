package util;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import util.Car.Direction;

/**
 * Represents a state in the rush hour puzzle.
 */
public class State {
    public Map<Character, Car> cars;
    public State parent;
    public String move;
    public long[] occupied;
    public int cost;

    /**
     * Creates a new state with the specified parameters.
     * 
     * @param cars Map of car IDs to Car objects
     * @param parent Parent state that led to this state
     * @param move Description of the move that created this state
     * @param cost Path cost to reach this state
     */
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

    /**
     * Creates a copy of the current state with a new parent and move.
     * 
     * @param newParent Parent state for the new copy
     * @param newMove Move description for the new copy
     * @return A new State object with incremented cost
     */
    public State copy(State newParent, String newMove) {
        Map<Character, Car> newCars = new HashMap<>();
        for (Map.Entry<Character, Car> entry : cars.entrySet()) {
            newCars.put(entry.getKey(), entry.getValue().copy());
        }
        return new State(newCars, newParent, newMove, cost + 1);
    }

    /**
     * Builds a bit mask representing all occupied cells in the puzzle.
     * 
     * @param cars Map of cars on the board
     * @param width Width of the puzzle grid
     * @param height Height of the puzzle grid
     * @return Long array representing occupied cells
     */
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

    /**
     * Checks if the primary car can reach the exit in the current state.
     * 
     * @param width Width of the puzzle grid
     * @param height Height of the puzzle grid
     * @param kRow Row position of the exit
     * @param kCol Column position of the exit
     * @param exitDirection Direction of the exit path
     * @return True if goal state is reached, false otherwise
     */
    public boolean isReached(int width, int height, int kRow, int kCol, String exitDirection) {
        Car primaryCar = this.cars.get('P');
        if (primaryCar == null) return false;
        
        // Check correct orientation for the specified exit direction
        if (primaryCar.isHorizontal) {
            // Horizontal car can only exit left or right
            if (!("right".equals(exitDirection) || "left".equals(exitDirection))) return false;
            
            // Find P's leftmost and rightmost positions
            int pLeftmostCol = -1;
            int pRightmostCol = -1;
            
            for (int c = 0; c < width; c++) {
                int idx = primaryCar.row * width + c;
                int chunk = idx / 64;
                int bit = idx % 64;
                
                if (chunk < primaryCar.bitmask.length && (primaryCar.bitmask[chunk] & (1L << bit)) != 0) {
                    if (pLeftmostCol == -1) pLeftmostCol = c;
                    pRightmostCol = c;
                }
            }
            
            // Check clear path to exit
            if ("right".equals(exitDirection)) {
                // Check if path to the right edge is clear
                for (int c = pRightmostCol + 1; c < width; c++) {
                    int idx = primaryCar.row * width + c;
                    int chunk = idx / 64;
                    int bit = idx % 64;
                    
                    if (chunk < occupied.length && (occupied[chunk] & (1L << bit)) != 0) {
                        return false; // Blocked
                    }
                }
                return true; // Clear path to right edge
            } else { // left
                // Check if path to the left edge is clear
                for (int c = 0; c < pLeftmostCol; c++) {
                    int idx = primaryCar.row * width + c;
                    int chunk = idx / 64;
                    int bit = idx % 64;
                    
                    if (chunk < occupied.length && (occupied[chunk] & (1L << bit)) != 0) {
                        return false; // Blocked
                    }
                }
                return true; // Clear path to left edge
            }
        } else { // Vertical car
            // Vertical car can only exit top or bottom
            if (!("top".equals(exitDirection) || "bottom".equals(exitDirection))) return false;
            
            // Find P's topmost and bottommost positions
            int pTopmostRow = -1;
            int pBottommostRow = -1;
            
            for (int r = 0; r < height; r++) {
                int idx = r * width + primaryCar.col;
                int chunk = idx / 64;
                int bit = idx % 64;
                
                if (chunk < primaryCar.bitmask.length && (primaryCar.bitmask[chunk] & (1L << bit)) != 0) {
                    if (pTopmostRow == -1) pTopmostRow = r;
                    pBottommostRow = r;
                }
            }
            
            // Check clear path to exit
            if ("bottom".equals(exitDirection)) {
                // Check if path to the bottom edge is clear
                for (int r = pBottommostRow + 1; r < height; r++) {
                    int idx = r * width + primaryCar.col;
                    int chunk = idx / 64;
                    int bit = idx % 64;
                    
                    if (chunk < occupied.length && (occupied[chunk] & (1L << bit)) != 0) {
                        return false; // Blocked
                    }
                }
                return true; // Clear path to bottom edge
            } else { // top
                // Check if path to the top edge is clear
                for (int r = 0; r < pTopmostRow; r++) {
                    int idx = r * width + primaryCar.col;
                    int chunk = idx / 64;
                    int bit = idx % 64;
                    
                    if (chunk < occupied.length && (occupied[chunk] & (1L << bit)) != 0) {
                        return false; // Blocked
                    }
                }
                return true; // Clear path to top edge
            }
        }
    }

    /**
     * Generates all valid next states by moving each car in all possible directions.
     * 
     * @param width Width of the puzzle grid
     * @param height Height of the puzzle grid
     * @return List of valid successor states
     */
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

    /**
     * Checks if a car collides with any other car when moved.
     * 
     * @param moved The moved car to check
     * @param occupied Bit mask of occupied cells
     * @param ignoreId ID of the car being moved
     * @return True if collision detected, false otherwise
     */
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

    /**
     * Generates a hash code for this state based on car positions.
     * 
     * @return Hash code value for this state
     */
    @Override
    public int hashCode() {
        return cars.hashCode();
    }

    /**
     * Compares this state with another state for equality.
     * 
     * @param obj Object to compare with
     * @return True if states are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof State other)) return false;
        return this.cars.equals(other.cars);
    }

    /**
     * Gets the complete history of moves that led to this state.
     * 
     * @return List of move descriptions in order from initial state
     */
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