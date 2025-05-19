import util.Parser;
import util.State;
import pathfinding.*;
import util.Car;
import util.BoardPrinter;
import heuristic.*;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class MainCLI {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the input file path: ");
        String inputFile = scanner.nextLine();
        
        try {

            Parser.ParsedResult parsed = Parser.parseFile(inputFile);
            State root = parsed.initialState;
            
            // What algorithm to use?
            System.out.println("Choose the algorithm to use:");
            System.out.println("1. A*");
            System.out.println("2. UCS");
            System.out.println("3. Greedy Best-First Search");
            System.out.println("4. Iterative Deepening A*");
            System.out.print("Enter your choice (1-4): ");
            int choice = scanner.nextInt();
            if (choice < 1 || choice > 4) {
                System.out.println("Invalid choice. Defaulting to UCS.");
                choice = 2;
            }
            
            // If using A* or Greedy, ask for heuristic
            Heuristic selectedHeuristic = null;
            if (choice == 1 || choice == 3 || choice == 4 || choice == 5) {
                System.out.println("\nChoose a heuristic:");
                System.out.println("1. Distance to exit");
                System.out.println("2. Number of blocking cars");
                System.out.println("3. Combined (distance + blocking cars)");
                System.out.println("4. Mobility Score");
                System.out.println("5. Blocking Car Distance");
                System.out.print("Enter your choice (1, 2, 3, 4, or 5): ");
                int heuristicChoice = scanner.nextInt();
                
                switch (heuristicChoice) {
                    case 1:
                        selectedHeuristic = new Distance();
                        break;
                    case 2:
                        selectedHeuristic = new BlockingCars();
                        break;
                    case 3:
                        selectedHeuristic = new CombinedHeuristic();
                        break;
                    case 4: 
                        selectedHeuristic = new MobilityScore(); 
                        break;
                    case 5: 
                        selectedHeuristic = new BlockingCarDistance(); 
                        break;
                    default:
                        System.out.println("Invalid choice. Using Distance heuristic.");
                        selectedHeuristic = new Distance();
                }
            }

            State goalState = null;
            
            long startTime = System.currentTimeMillis();
            if (choice == 1) {
                // A* algorithm
                System.out.println("\nUsing A* algorithm...");
                AStar solver = new AStar(parsed.width, parsed.height, parsed.kRow, parsed.kCol, parsed.exitDirection, selectedHeuristic);
                goalState = solver.find(root);
            } else if (choice == 2) {
                // UCS algorithm
                System.out.println("\nUsing UCS algorithm...");
                UCS solver = new UCS(parsed.width, parsed.height, parsed.kRow, parsed.kCol, parsed.exitDirection);
                goalState = solver.find(root);
            } else if (choice == 3) {
                // Greedy Best-First Search algorithm
                System.out.println("\nUsing Greedy Best-First Search algorithm...");
                GreedyBFS solver = new GreedyBFS(parsed.width, parsed.height, parsed.kRow, parsed.kCol, parsed.exitDirection, selectedHeuristic);
                goalState = solver.find(root);
            } else {
                // Iterative Deepening A* algorithm
                System.out.println("\nUsing Iterative Deepening A* algorithm...");
                IDAStar solver = new IDAStar(parsed.width, parsed.height, parsed.kRow, parsed.kCol, parsed.exitDirection, selectedHeuristic);
                goalState = solver.find(root);
            }
            long endTime = System.currentTimeMillis();
            
            if (goalState != null) {
                System.out.println("\nSolution Path:");
                
                // Build a list of states in order from initial to goal
                List<State> statePath = new ArrayList<>();
                State currentState = goalState;
                while (currentState != null) {
                    statePath.add(currentState);
                    currentState = currentState.parent;
                }
                Collections.reverse(statePath);
                
                // Print initial state
                System.out.println("Initial state:");
                BoardPrinter.printBoard(statePath.get(0), parsed.width, parsed.height);
                System.out.println();
                
                // Print each move and resulting state
                for (int i = 1; i < statePath.size(); i++) {
                    State state = statePath.get(i);
                    System.out.println("Move " + i + ": " + state.move);
                    BoardPrinter.printBoard(state, parsed.width, parsed.height);
                    System.out.println();
                }
                
                System.out.println("Total moves: " + (statePath.size() - 1));
                System.out.println("\nFinal Board State:");
                BoardPrinter.printBoard(goalState, parsed.width, parsed.height);
            }

            System.out.printf("\nExecution time: %.3f seconds\n", (endTime - startTime) / 1000.0);

        } catch (Exception e) {
            System.out.println("Error while processing: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}