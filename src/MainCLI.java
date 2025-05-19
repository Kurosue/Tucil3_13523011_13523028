import util.Parser;
import util.State;
import pathfinding.UCS;
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
            long[] goalMask = parsed.goalMask;
                        
            UCS solver = new UCS(goalMask, parsed.width, parsed.height);
            State goalState = solver.find(root);

            if (goalState != null) {
                System.out.println("\nSolution Path:");
                for (String move : goalState.getMoveHistory()) {
                    System.out.println(move);
                }
            }
            System.out.println("\nFinal Board State:");
            
            long endTime = System.currentTimeMillis();
            System.out.printf("\nExecution time: %.3f seconds\n", (endTime - startTime) / 1000.0);

        } catch (Exception e) {
            System.out.println("Error while processing: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}