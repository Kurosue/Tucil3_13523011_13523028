import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            // No arguments provided, display usage information
            System.out.println("Rush Hour Puzzle Solver");
            System.out.println("Usage: java -jar RushHourSolver.jar [mode]");
            System.out.println("Available modes:");
            System.out.println("  cli   - Run in command line interface mode");
            System.out.println("  gui   - Run in graphical user interface mode (default)");
            
            // Default to GUI mode if no arguments
            System.out.println("GUI or CLI mode?");
            Scanner inputScanner = new Scanner(System.in);
            while (true) {
                System.out.print("Enter 'cli' for CLI mode or 'gui' for GUI mode: ");
                String input = inputScanner.nextLine();
                if (input.equalsIgnoreCase("cli")) {
                    System.out.println("Starting Rush Hour Solver in CLI mode...");
                    MainCLI.main(new String[0]);
                    inputScanner.close();
                    break;
                } else if (input.equalsIgnoreCase("gui")) {
                    System.out.println("Starting Rush Hour Solver in GUI mode...");
                    MainGUI.main(new String[0]);
                    break;
                } else {
                    System.err.println("Error: Unknown mode '" + input + "'");
                    System.out.println("Available modes: 'cli' or 'gui'");
                }
            }            
        } else if (args.length == 1) {
            String mode = args[0].toLowerCase();
            
            switch (mode) {
                case "cli":
                    System.out.println("Starting Rush Hour Solver in CLI mode...");
                    MainCLI.main(new String[0]);
                    break;
                    
                case "gui":
                    System.out.println("Starting Rush Hour Solver in GUI mode...");
                    MainGUI.main(new String[0]);
                    break;
                    
                default:
                    System.err.println("Error: Unknown mode '" + mode + "'");
                    System.out.println("Available modes: 'cli' or 'gui'");
                    System.exit(1);
            }
            
        } else {
            // Too many arguments
            System.err.println("Error: Too many arguments");
            System.out.println("Usage: java -jar RushHourSolver.jar [mode]");
            System.out.println("Available modes: 'cli' or 'gui'");
            System.exit(1);
        }
    }
}