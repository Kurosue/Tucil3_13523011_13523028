import util.Parser;
import util.State;
import pathfinding.*;
import util.Car;
import util.BoardPrinter;
import heuristic.*;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.InputMismatchException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainCLI {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        try {
            System.out.print("Enter the input file path: ");
            String inputFile = scanner.nextLine();
            
            // Validate file path
            File file = new File(inputFile);
            if (!file.exists()) {
                throw new IOException("File does not exist: " + inputFile);
            }
            if (!file.isFile()) {
                throw new IOException("Path is not a file: " + inputFile);
            }
            if (!file.canRead()) {
                throw new IOException("Cannot read file (check permissions): " + inputFile);
            }

            // Create output file name based on input file name
            Path inputPath = Paths.get(inputFile);
            String inputFileName = inputPath.getFileName().toString();
            String baseName = inputFileName;
            int dotIndex = baseName.lastIndexOf('.');
            if (dotIndex > 0) {
                baseName = baseName.substring(0, dotIndex);
            }
            
            Parser.ParsedResult parsed;
            try {
                parsed = Parser.parseFile(inputFile);
                System.out.println("File parsed successfully.");
                System.out.println("Board size: " + parsed.width + " x " + parsed.height);
                System.out.println("Exit direction: " + parsed.exitDirection);
            } catch (IOException ex) {
                throw new IOException("Error reading file: " + ex.getMessage());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid file format: " + ex.getMessage());
            }
            
            State root = parsed.initialState;
            
            if (root == null || root.cars == null || root.cars.isEmpty()) {
                throw new IllegalStateException("No valid initial state was parsed from the file");
            }
            
            // Validate primary car exists
            if (!root.cars.containsKey('P')) {
                throw new IllegalStateException("Missing primary car (P) in the puzzle configuration");
            }
            
            // Choose algorithm with error handling
            int choice;
            try {
                System.out.println("\nChoose the algorithm to use:");
                System.out.println("1. A*");
                System.out.println("2. UCS");
                System.out.println("3. Greedy Best-First Search");
                System.out.println("4. Iterative Deepening A*");
                System.out.print("Enter your choice (1-4): ");
                choice = scanner.nextInt();
                
                if (choice < 1 || choice > 4) {
                    System.out.println("Invalid choice. Defaulting to UCS (option 2).");
                    choice = 2;
                }
            } catch (InputMismatchException ex) {
                System.out.println("Invalid input. Defaulting to UCS (option 2).");
                choice = 2;
                scanner.nextLine(); // Clear the scanner buffer
            }
            
            // Define algorithm name for output file - ADD THIS CODE HERE
            String algorithmName = "";
            switch (choice) {
                case 1:
                    algorithmName = "AStar";
                    break;
                case 2:
                    algorithmName = "UCS";
                    break;
                case 3:
                    algorithmName = "GreedyBFS";
                    break;
                case 4:
                    algorithmName = "IDAStar";
                    break;
                default:
                    algorithmName = "Unknown";
            }
            
            String outputFileName = "output_" + algorithmName + "_" + baseName + ".txt";
            
            // If using A* or Greedy, ask for heuristic with error handling
            Heuristic selectedHeuristic = null;
            if (choice == 1 || choice == 3 || choice == 4) {
                try {
                    System.out.println("\nChoose a heuristic:");
                    System.out.println("1. Distance to exit");
                    System.out.println("2. Number of blocking cars");
                    System.out.println("3. Combined (distance + blocking cars)");
                    System.out.println("4. Mobility Score");
                    System.out.println("5. Blocking Car Distance");
                    System.out.print("Enter your choice (1-5): ");
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
                } catch (InputMismatchException ex) {
                    System.out.println("Invalid input. Using Distance heuristic.");
                    selectedHeuristic = new Distance();
                    scanner.nextLine(); // Clear the scanner buffer
                }
            }

            State goalState = null;
            
            System.out.println("\nSolving puzzle...");
            
            // Create a StringBuilder to capture output for the file
            StringBuilder outputContent = new StringBuilder();
            outputContent.append("Solving puzzle...\n");
            
            long startTime = System.currentTimeMillis();
                        int visitedNodeCount = 0;
                        try {
                switch (choice) {
                    case 1:
                        // A* algorithm
                        System.out.println("Using A* algorithm with " + selectedHeuristic.getName() + " heuristic...");
                        outputContent.append("Using A* algorithm with " + selectedHeuristic.getName() + " heuristic...\n");
                        AStar solver = new AStar(parsed.width, parsed.height, parsed.kRow, parsed.kCol, parsed.exitDirection, selectedHeuristic);
                        goalState = solver.find(root);
                        visitedNodeCount = solver.getVisitedNodeCount();
                        break;
                    case 2:
                        // UCS algorithm
                        System.out.println("Using UCS algorithm...");
                        outputContent.append("Using UCS algorithm...\n");
                        UCS solver2 = new UCS(parsed.width, parsed.height, parsed.kRow, parsed.kCol, parsed.exitDirection);
                        goalState = solver2.find(root);
                        visitedNodeCount = solver2.getVisitedNodeCount();
                        break;
                    case 3:
                        // Greedy Best-First Search algorithm
                        System.out.println("Using Greedy Best-First Search with " + selectedHeuristic.getName() + " heuristic...");
                        outputContent.append("Using Greedy Best-First Search with " + selectedHeuristic.getName() + " heuristic...\n");
                        GreedyBFS solver3 = new GreedyBFS(parsed.width, parsed.height, parsed.kRow, parsed.kCol, parsed.exitDirection, selectedHeuristic);
                        goalState = solver3.find(root);
                        visitedNodeCount = solver3.getVisitedNodeCount();
                        break;
                    case 4:
                        // Iterative Deepening A* algorithm
                        System.out.println("Using Iterative Deepening A* with " + selectedHeuristic.getName() + " heuristic...");
                        outputContent.append("Using Iterative Deepening A* with " + selectedHeuristic.getName() + " heuristic...\n");
                        IDAStar solver4 = new IDAStar(parsed.width, parsed.height, parsed.kRow, parsed.kCol, parsed.exitDirection, selectedHeuristic);
                        goalState = solver4.find(root);
                        visitedNodeCount = solver4.getVisitedNodeCount();
                        break;
                    default:
                        throw new IllegalStateException("Invalid algorithm choice: " + choice);
                }
            } catch (OutOfMemoryError e) {
                throw new RuntimeException("Out of memory while solving the puzzle. Try a smaller puzzle or a different algorithm.");
            } catch (Exception e) {
                throw new RuntimeException("Error solving the puzzle: " + e.getMessage());
            }
            
            System.out.println("Nodes visited: " + visitedNodeCount);
            outputContent.append("Nodes visited: " + visitedNodeCount + "\n");
            
            
            long endTime = System.currentTimeMillis();
            double executionTime = (endTime - startTime) / 1000.0;

            System.out.printf("\nExecution time: %.3f seconds\n", executionTime);
            outputContent.append(String.format("\nExecution time: %.3f seconds\n", executionTime));
            
            if (goalState != null) {
                System.out.println("\nSolution Path:");
                outputContent.append("\nSolution Path:\n");
                
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
                outputContent.append("Initial state:\n");
                
                // Capture board state as string
                String initialBoard = boardToString(statePath.get(0), parsed.width, parsed.height);
                System.out.println(initialBoard);
                outputContent.append(initialBoard).append("\n\n");
                
                // Print each move and resulting state
                for (int i = 1; i < statePath.size(); i++) {
                    State state = statePath.get(i);
                    System.out.println("Move " + i + ": " + state.move);
                    outputContent.append("Move " + i + ": " + state.move + "\n");
                    
                    String boardState = boardToString(state, parsed.width, parsed.height);
                    System.out.println(boardState);
                    outputContent.append(boardState).append("\n\n");
                }
                
                System.out.println("Total moves: " + (statePath.size() - 1));
                outputContent.append("Total moves: " + (statePath.size() - 1) + "\n");
                
                System.out.println("\nFinal Board State:");
                outputContent.append("\nFinal Board State:\n");
                
                String finalBoard = boardToString(goalState, parsed.width, parsed.height);
                System.out.println(finalBoard);
                outputContent.append(finalBoard).append("\n");
            } else {
                System.out.println("\nNo solution found for this puzzle configuration.");
                outputContent.append("\nNo solution found for this puzzle configuration.\n");
            }

            System.out.printf("\nExecution time: %.3f seconds\n", executionTime);
            outputContent.append(String.format("\nExecution time: %.3f seconds\n", executionTime));


            File outputDir = new File("test");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            String outputFilePath = "test/" + outputFileName;
            try (PrintWriter writer = new PrintWriter(new FileWriter(outputFilePath))) {
                writer.print(outputContent.toString());
                System.out.println("\nOutput saved to " + outputFilePath);
            } catch (IOException e) {
                System.err.println("Error writing to output file: " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("Error with input file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid input format: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Problem with puzzle state: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Runtime error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                scanner.close();
            } catch (Exception e) {
                // Ignore errors while closing scanner
            }
            System.out.println("\nProgram terminated.");
        }
    }
    
    /**
     * Converts a board state to a string representation for file output
     */
    private static String boardToString(State state, int width, int height) {
        StringBuilder sb = new StringBuilder();
        
        char[][] board = new char[height][width];
        
        // Initialize board with empty spaces
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                board[i][j] = '.';
            }
        }
        
        // Place cars on the board
        for (Map.Entry<Character, Car> entry : state.cars.entrySet()) {
            char carId = entry.getKey();
            Car car = entry.getValue();
            
            for (int i = 0; i < car.bitmask.length; i++) {
                long chunk = car.bitmask[i];
                for (int b = 0; b < 64; b++) {
                    if ((chunk & (1L << b)) != 0) {
                        int idx = i * 64 + b;
                        int row = idx / width;
                        int col = idx % width;
                        if (row < height && col < width) {
                            board[row][col] = carId;
                        }
                    }
                }
            }
        }
        
        // Build string representation
        sb.append("+" + "-".repeat(width) + "+\n");
        for (int i = 0; i < height; i++) {
            sb.append("|");
            for (int j = 0; j < width; j++) {
                sb.append(board[i][j]);
            }
            sb.append("|\n");
        }
        sb.append("+" + "-".repeat(width) + "+");
        
        return sb.toString();
    }
}