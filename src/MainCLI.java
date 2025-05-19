import util.Parser;
import util.State;
import pathfinding.UCS;
import pathfinding.AStar;
import util.Car;
import util.BoardPrinter;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class MainCLI {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the input file path: ");
        String inputFile = scanner.nextLine();
        
        try {
            long startTime = System.currentTimeMillis();

            Parser.ParsedResult parsed = Parser.parseFile(inputFile);
            State root = parsed.initialState;
            
            // Create goalMask based on board dimensions
            // This assumes the goal is to move the primary piece to the right edge
            int width = parsed.width;
            int height = parsed.height;
            int totalBits = width * height;
            int chunkCount = (totalBits + 63) / 64;
            
            // Determine goal location (typically far right of middle row)
            int goalRow = height / 2;
            long[] goalMask = new long[chunkCount];
            
            // Set bits for goal position (typically 2 cells at the exit)
            int goalPos1 = goalRow * width + (width - 2);
            int goalPos2 = goalRow * width + (width - 1);
            
            goalMask[goalPos1 / 64] |= (1L << (goalPos1 % 64));
            goalMask[goalPos2 / 64] |= (1L << (goalPos2 % 64));
            
            // What algorithm to use?
            System.out.println("Choose the algorithm to use:");
            System.out.println("1. A*");
            System.out.println("2. UCS");
            System.out.print("Enter your choice (1 or 2): ");
            int choice = scanner.nextInt();
            if (choice != 1 && choice != 2) {
                System.out.println("Invalid choice. Defaulting to UCS.");
                choice = 2;
            }

            if (choice == 1) {
                // A* algorithm
                System.out.println("Using A* algorithm...");
                AStar solver = new AStar(goalMask, parsed.width, parsed.height, parsed.kRow, parsed.kCol, parsed.exitDirection);
                State goalState = solver.find(root);
                
                if (goalState != null) {
                    System.out.println("\nSolution Path:");
                    for (String move : goalState.getMoveHistory()) {
                        System.out.println(move);
                    }
                    System.out.println("\nFinal Board State:");
                    BoardPrinter.printBoard(goalState, parsed.width, parsed.height, goalPos1, goalPos2);
                }
            } else {
                // UCS algorithm
                System.out.println("Using UCS algorithm...");
                UCS solver = new UCS(parsed.width, parsed.height, parsed.kRow, parsed.kCol, parsed.exitDirection);
                State goalState = solver.find(root);
                if (goalState != null) {
                    System.out.println("\nSolution Path:");
                    for (String move : goalState.getMoveHistory()) {
                        System.out.println(move);
                    }
                    System.out.println("\nFinal Board State:");
                    BoardPrinter.printBoard(goalState, parsed.width, parsed.height, goalPos1, goalPos2);
                }
            }            

            long endTime = System.currentTimeMillis();
            System.out.printf("\nExecution time: %.3f milliseconds\n", (endTime - startTime) / 1000.0);

        } catch (Exception e) {
            System.out.println("Error while processing: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}