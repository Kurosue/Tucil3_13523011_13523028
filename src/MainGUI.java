import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;
import javafx.scene.paint.Color;
import javafx.collections.FXCollections;
import javafx.scene.shape.Rectangle;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import util.*;
import heuristic.*;
import pathfinding.*;

public class MainGUI extends Application {
    private GridPane boardPane;
    private int boardWidth = 6;
    private int boardHeight = 6;
    private int numPieces = 5;
    private char currentColor = 'P';  // Default to primary car
    private Button[][] boardButtons;
    private Map<Character, Color> colorMap = new HashMap<>();
    private boolean isPrimaryHorizontal = true; // Horizontal by default
    private int exitRow = 2;  // Default exit position
    private int exitCol = 5;  // Default at right edge
    private String exitDirection = "right"; // Default exit direction
    private List<State> solutionStates = new ArrayList<>();
    private int currentStepIndex = 0;
    private Timeline animationTimeline;
    private State currentState;  // Current board state
    
    // New fields for algorithm selection
    private String selectedAlgorithm = "A*";
    private Heuristic selectedHeuristic;
    private Label lblVisitedNodes;
    private Label lblExecutionTime;
    private Label lblStep;
    private Button btnPrev;
    private Button btnPlay;
    private Button btnNext;
    private Button btnSaveResultToText;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Rush Hour Puzzle Solver");
    
        // Initialize layout components
        boardPane = new GridPane();
        boardPane.setHgap(2);
        boardPane.setVgap(2);
        boardPane.setAlignment(Pos.CENTER);
        boardPane.setPadding(new Insets(10));
    
        lblVisitedNodes = new Label("Nodes Visited: 0");
        lblExecutionTime = new Label("Execution Time: 0.000 seconds");
        lblStep = new Label("Step: 0/0");
    
        // Main layout with three sections: left panel, board, right panel
        HBox root = new HBox(15);  
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER); // Center the HBox in the window
        
        // Create left panel for configuration
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(5));
        leftPanel.setPrefWidth(250);
        leftPanel.setMinWidth(250);
        leftPanel.setMaxWidth(250);
        leftPanel.setAlignment(Pos.TOP_CENTER);
    
        // Create right panel for editing and controls
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(5));
        rightPanel.setPrefWidth(250);
        rightPanel.setMinWidth(250);
        rightPanel.setMaxWidth(250);
        rightPanel.setAlignment(Pos.TOP_CENTER);
    
        // Configure board pane to be centered
        StackPane boardContainer = new StackPane(boardPane);
        boardContainer.setAlignment(Pos.CENTER);
        boardContainer.setMinWidth(300);
        boardContainer.setPrefWidth(Region.USE_COMPUTED_SIZE);
    
        // Board size configuration
        Label lblBoardConfig = new Label("Board Configuration");
        lblBoardConfig.setStyle("-fx-font-weight: bold; -fx-underline: true;");
        
        HBox sizeConfig = new HBox(10);
        sizeConfig.setAlignment(Pos.CENTER);
        Label lblWidth = new Label("Width:");
        Spinner<Integer> widthSpinner = new Spinner<>(3, 20, boardWidth);
        widthSpinner.setEditable(true);
        widthSpinner.setPrefWidth(70);
        Label lblHeight = new Label("Height:");
        Spinner<Integer> heightSpinner = new Spinner<>(3, 20, boardHeight);
        heightSpinner.setEditable(true);
        heightSpinner.setPrefWidth(70);
        sizeConfig.getChildren().addAll(lblWidth, widthSpinner, lblHeight, heightSpinner);
    
        // Algorithm selection
        Label lblSolverConfig = new Label("Solver Configuration");
        lblSolverConfig.setStyle("-fx-font-weight: bold; -fx-underline: true;");
        
        VBox algorithmConfig = new VBox(5);
        algorithmConfig.setAlignment(Pos.CENTER_LEFT);
        Label lblAlgorithm = new Label("Pathfinding Algorithm:");
        ComboBox<String> algorithmComboBox = new ComboBox<>();
        algorithmComboBox.setMaxWidth(Double.MAX_VALUE);
        algorithmComboBox.setItems(FXCollections.observableArrayList(
            "A*", "Uniform Cost Search (UCS)", "Greedy Best-First Search", "Iterative Deepening A*"
        ));
        algorithmComboBox.getSelectionModel().selectFirst();
        algorithmConfig.getChildren().addAll(lblAlgorithm, algorithmComboBox);
        
        // Heuristic selection
        VBox heuristicConfig = new VBox(5);
        heuristicConfig.setAlignment(Pos.CENTER_LEFT);
        Label lblHeuristic = new Label("Heuristic Function:");
        ComboBox<String> heuristicComboBox = new ComboBox<>();
        heuristicComboBox.setMaxWidth(Double.MAX_VALUE);
        heuristicComboBox.setItems(FXCollections.observableArrayList(
            "Distance to Exit", 
            "Number of Blocking Cars", 
            "Combined (Distance + Blocking)",
            "Mobility Score",
            "Blocking Car Distance"
        ));
        heuristicComboBox.getSelectionModel().selectFirst();
        heuristicConfig.getChildren().addAll(lblHeuristic, heuristicComboBox);
        
        // Primary piece orientation
        VBox orientationConfig = new VBox(5);
        orientationConfig.setAlignment(Pos.CENTER_LEFT);
        Label lblOrientation = new Label("Primary Piece Orientation:");
        HBox orientationButtons = new HBox(10);
        orientationButtons.setAlignment(Pos.CENTER);
        ToggleGroup orientationGroup = new ToggleGroup();
        RadioButton rbHorizontal = new RadioButton("Horizontal");
        rbHorizontal.setToggleGroup(orientationGroup);
        rbHorizontal.setSelected(true);
        RadioButton rbVertical = new RadioButton("Vertical");
        rbVertical.setToggleGroup(orientationGroup);
        orientationButtons.getChildren().addAll(rbHorizontal, rbVertical);
        orientationConfig.getChildren().addAll(lblOrientation, orientationButtons);
        
        // Exit gate position controls
        VBox exitGateConfig = new VBox(5);
        exitGateConfig.setAlignment(Pos.CENTER_LEFT);
        Label lblExitGate = new Label("Exit Position:");
        Spinner<Integer> exitPositionSpinner = new Spinner<>(0, boardHeight-1, exitRow);
        exitPositionSpinner.setEditable(true);
        exitPositionSpinner.setMaxWidth(Double.MAX_VALUE);
        exitGateConfig.getChildren().addAll(lblExitGate, exitPositionSpinner);
        
        // Apply button to set up the board
        Button btnApplyConfig = new Button("Apply Configuration");
        btnApplyConfig.setMaxWidth(Double.MAX_VALUE);
    
        // ===== COMPONENTS FOR RIGHT PANEL =====
        // Color selection
        Label lblBoardEditor = new Label("Board Editor");
        lblBoardEditor.setStyle("-fx-font-weight: bold; -fx-underline: true;");
        
        VBox colorSelection = new VBox(5);
        colorSelection.setAlignment(Pos.CENTER_LEFT);
        Label lblColor = new Label("Select Piece:");
        ComboBox<String> colorComboBox = new ComboBox<>();
        colorComboBox.setMaxWidth(Double.MAX_VALUE);
        colorComboBox.setItems(FXCollections.observableArrayList("Primary (Red)"));
        colorSelection.getChildren().addAll(lblColor, colorComboBox);
    
        // Create legend panel to show colors
        Label legendTitle = new Label("Color Legend:");
        FlowPane colorLegend = new FlowPane();
        colorLegend.setHgap(5);
        colorLegend.setVgap(5);
        colorLegend.setAlignment(Pos.CENTER);
        colorLegend.setPrefWrapLength(200);
        
        // Load and solve buttons
        Label lblOperations = new Label("Operations");
        lblOperations.setStyle("-fx-font-weight: bold; -fx-underline: true;");
        
        Button btnLoadFromFile = new Button("Load From File");
        btnLoadFromFile.setMaxWidth(Double.MAX_VALUE);
        
        Button btnSolve = new Button("Solve Puzzle");
        btnSolve.setMaxWidth(Double.MAX_VALUE);
        
        btnSaveResultToText = new Button("Save Solution to Text");
        btnSaveResultToText.setMaxWidth(Double.MAX_VALUE);
        btnSaveResultToText.setDisable(true);
        
        // Solution navigation controls
        Label lblSolution = new Label("Solution Navigation");
        lblSolution.setStyle("-fx-font-weight: bold; -fx-underline: true;");
        
        HBox navigationControls = new HBox(10);
        navigationControls.setAlignment(Pos.CENTER);
        btnPrev = new Button("◀");
        btnPlay = new Button("▶");
        btnNext = new Button("▶▶");
        navigationControls.getChildren().addAll(btnPrev, btnPlay, btnNext);
        
        btnPrev.setDisable(true);
        btnPlay.setDisable(true);
        btnNext.setDisable(true);
    
        // Initialize color map
        initializeColorMap();
        
        // Add configuration components to the left panel
        leftPanel.getChildren().addAll(
            lblBoardConfig,
            sizeConfig, 
            new Separator(),
            lblSolverConfig,
            algorithmConfig,
            heuristicConfig,
            orientationConfig,
            exitGateConfig,
            btnApplyConfig
        );
        
        // Add editor and controls to the right panel
        rightPanel.getChildren().addAll(
            lblBoardEditor,
            colorSelection,
            legendTitle,
            colorLegend,
            new Separator(),
            lblOperations,
            btnLoadFromFile,
            btnSolve,
            btnSaveResultToText,
            new Separator(),
            lblSolution,
            navigationControls,
            lblStep,
            lblVisitedNodes, 
            lblExecutionTime
        );
    
        // Add all three components to the main layout
        root.getChildren().addAll(leftPanel, boardContainer, rightPanel);
        
        // Create initial state
        selectedHeuristic = new Distance(); // Default heuristic
        initializeEmptyState();
        
        // Event handlers for apply config button
        btnApplyConfig.setOnAction(e -> {
            boardWidth = widthSpinner.getValue();
            boardHeight = heightSpinner.getValue();
            selectedAlgorithm = algorithmComboBox.getValue();
            isPrimaryHorizontal = rbHorizontal.isSelected();
            
            // Update selected heuristic based on combo box
            switch(heuristicComboBox.getValue()) {
                case "Distance to Exit":
                    selectedHeuristic = new Distance();
                    break;
                case "Number of Blocking Cars":
                    selectedHeuristic = new BlockingCars();
                    break;
                case "Combined (Distance + Blocking)":
                    selectedHeuristic = new CombinedHeuristic();
                    break;
                case "Mobility Score":
                    selectedHeuristic = new MobilityScore();
                    break;
                case "Blocking Car Distance":
                    selectedHeuristic = new BlockingCarDistance();
                    break;
                default:
                    selectedHeuristic = new Distance();
                    break;
            }
            
            // Update exit position spinner max value based on board dimensions
            int maxPos = isPrimaryHorizontal ? boardHeight-1 : boardWidth-1;
            exitPositionSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, maxPos, 
                    Math.min(exitPositionSpinner.getValue(), maxPos)));
            
            // Set exit gate position based on orientation
            if (isPrimaryHorizontal) {
                exitRow = exitPositionSpinner.getValue();
                exitCol = boardWidth - 1;
                exitDirection = "right";
            } else {
                exitRow = boardHeight - 1;
                exitCol = exitPositionSpinner.getValue();
                exitDirection = "bottom";
            }
            
            // Rebuild color dropdown with available vehicles
            colorComboBox.getItems().clear();
            colorComboBox.getItems().add("Primary (Red)");
            for (char c = 'A'; c < 'A' + numPieces && c <= 'Z'; c++) {
                colorComboBox.getItems().add("Vehicle " + c);
            }
            colorComboBox.getSelectionModel().selectFirst();
            currentColor = 'P'; // Reset to primary car
            
            // Initialize the board
            initializeEmptyState();
            initializeBoard();
            
            // Reset solution steps
            solutionStates.clear();
            currentStepIndex = 0;
            lblStep.setText("Step: 0/0");
            btnPrev.setDisable(true);
            btnPlay.setDisable(true);
            btnNext.setDisable(true);
            
            // Reset metrics
            lblVisitedNodes.setText("Nodes Visited: 0");
            lblExecutionTime.setText("Execution Time: 0.000 seconds");
            btnSaveResultToText.setDisable(true);
            
            // Update color legend
            updateColorLegend(colorLegend);
        });

        // Event listeners for orientation change
        rbHorizontal.setOnAction(e -> {
            isPrimaryHorizontal = true;
            exitDirection = "right";
            int maxPos = boardHeight - 1;
            exitPositionSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, maxPos, 
                    Math.min(exitPositionSpinner.getValue(), maxPos)));
            exitRow = exitPositionSpinner.getValue();
            exitCol = boardWidth - 1;
            initializeBoard();
        });
        
        rbVertical.setOnAction(e -> {
            isPrimaryHorizontal = false;
            exitDirection = "bottom";
            int maxPos = boardWidth - 1;
            exitPositionSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, maxPos, 
                    Math.min(exitPositionSpinner.getValue(), maxPos)));
            exitRow = boardHeight - 1;
            exitCol = exitPositionSpinner.getValue();
            initializeBoard();
        });
        
        // Exit position change listener
        exitPositionSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (isPrimaryHorizontal) {
                exitRow = newVal;
                exitCol = boardWidth - 1;
            } else {
                exitRow = boardHeight - 1;
                exitCol = newVal;
            }
            initializeBoard();
        });

        // Color selection handler
        colorComboBox.setOnAction(e -> {
            String selected = colorComboBox.getValue();
            if (selected != null) {
                if (selected.equals("Primary (Red)")) {
                    currentColor = 'P'; 
                } else {
                    // Extract the character from "Vehicle X"
                    currentColor = selected.charAt(selected.length() - 1);
                }
            }
        });

        // Solution navigation event handlers
        btnPrev.setOnAction(e -> {
            if (currentStepIndex > 0) {
                currentStepIndex--;
                showSolutionStep(currentStepIndex);
                lblStep.setText(String.format("Step: %d/%d", currentStepIndex, solutionStates.size() - 1));
                btnNext.setDisable(false);
                btnPlay.setDisable(false);
                if (currentStepIndex == 0) {
                    btnPrev.setDisable(true);
                }
            }
        });
        
        btnNext.setOnAction(e -> {
            if (currentStepIndex < solutionStates.size() - 1) {
                currentStepIndex++;
                showSolutionStep(currentStepIndex);
                lblStep.setText(String.format("Step: %d/%d", currentStepIndex, solutionStates.size() - 1));
                btnPrev.setDisable(false);
                if (currentStepIndex == solutionStates.size() - 1) {
                    btnNext.setDisable(true);
                    btnPlay.setDisable(true);
                }
            }
        });
        
        btnPlay.setOnAction(e -> {
            if (animationTimeline != null && animationTimeline.getStatus() == Timeline.Status.RUNNING) {
                animationTimeline.stop();
                btnPlay.setText("▶");
            } else {
                animationTimeline = new Timeline();
                animationTimeline.setCycleCount(solutionStates.size() - currentStepIndex - 1);
                
                KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), event -> {
                    if (currentStepIndex < solutionStates.size() - 1) {
                        currentStepIndex++;
                        showSolutionStep(currentStepIndex);
                        lblStep.setText(String.format("Step: %d/%d", currentStepIndex, solutionStates.size() - 1));
                        btnPrev.setDisable(false);
                        if (currentStepIndex == solutionStates.size() - 1) {
                            btnNext.setDisable(true);
                            btnPlay.setText("▶");
                            animationTimeline.stop();
                        }
                    }
                });
                
                animationTimeline.getKeyFrames().add(keyFrame);
                animationTimeline.play();
                btnPlay.setText("⏸");
            }
        });

        // Solve button
        btnSolve.setOnAction(e -> {
            // Get current board state
            try {
                // Start timing
                long startTime = System.currentTimeMillis();
                
                // Solve using the selected algorithm
                State solution = null;
                int visitedNodes = 0;
                
                switch (selectedAlgorithm) {
                    case "A*":
                        AStar astar = new AStar(boardWidth, boardHeight, exitRow, exitCol, exitDirection, selectedHeuristic);
                        solution = astar.find(currentState);
                        break;
                        
                    case "Uniform Cost Search (UCS)":
                        UCS ucs = new UCS(boardWidth, boardHeight, exitRow, exitCol, exitDirection);
                        solution = ucs.find(currentState);
                        break;
                        
                    case "Greedy Best-First Search":
                        GreedyBFS greedy = new GreedyBFS(boardWidth, boardHeight, exitRow, exitCol, exitDirection, selectedHeuristic);
                        solution = greedy.find(currentState);
                        break;
                        
                    case "Iterative Deepening A*":
                        IDAStar ida = new IDAStar(boardWidth, boardHeight, exitRow, exitCol, exitDirection, selectedHeuristic);
                        solution = ida.find(currentState);
                        break;
                        
                    default:
                        throw new IllegalStateException("Unknown algorithm selected: " + selectedAlgorithm);
                }
                
                long endTime = System.currentTimeMillis();
                double executionTime = (endTime - startTime) / 1000.0;
                
                // Update metrics
                lblExecutionTime.setText(String.format("Execution Time: %.3f seconds", executionTime));
                
                if (solution != null) {
                    // Build solution states list by traversing the solution path
                    solutionStates = buildSolutionPath(solution);
                    
                    // Update UI
                    lblVisitedNodes.setText("Nodes Visited: " + visitedNodes);
                    lblStep.setText(String.format("Step: 0/%d", solutionStates.size() - 1));
                    currentStepIndex = 0;
                    
                    // Enable navigation buttons
                    btnPrev.setDisable(true); // At step 0
                    btnPlay.setDisable(solutionStates.size() <= 1);
                    btnNext.setDisable(solutionStates.size() <= 1);
                    btnSaveResultToText.setDisable(false);
                    
                    // Show initial state
                    showSolutionStep(0);
                    
                    // Show success dialog
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Solution Found");
                    alert.setHeaderText("Puzzle Solved!");
                    alert.setContentText(String.format(
                        "Solution found in %d steps\nTime taken: %.3f seconds",
                        solutionStates.size() - 1, executionTime));
                    alert.showAndWait();
                } else {
                    // Show failure dialog
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("No Solution");
                    alert.setHeaderText("Could not solve the puzzle");
                    alert.setContentText("The algorithm could not find a solution for this puzzle configuration.");
                    alert.showAndWait();
                }
            } catch (Exception ex) {
                // Show error dialog
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("An error occurred");
                alert.setContentText("Error solving puzzle: " + ex.getMessage());
                ex.printStackTrace();
                alert.showAndWait();
            }
        });

        // File load button
        btnLoadFromFile.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Rush Hour Configuration File");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
            );
            
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    // Parse the file using our Parser class
                    Parser.ParsedResult parsed = Parser.parseFile(file.getAbsolutePath());
                    
                    // Update board dimensions and state
                    boardWidth = parsed.width;
                    boardHeight = parsed.height;
                    exitRow = parsed.kRow;
                    exitCol = parsed.kCol;
                    exitDirection = parsed.exitDirection;
                    currentState = parsed.initialState;
                    
                    // Update UI controls
                    widthSpinner.getValueFactory().setValue(boardWidth);
                    heightSpinner.getValueFactory().setValue(boardHeight);
                    
                    // Determine orientation from primary car
                    Car primaryCar = currentState.cars.get('P');
                    if (primaryCar != null) {
                        isPrimaryHorizontal = primaryCar.isHorizontal;
                        if (isPrimaryHorizontal) {
                            rbHorizontal.setSelected(true);
                        } else {
                            rbVertical.setSelected(true);
                        }
                    }
                    
                    // Update exit position spinner
                    int position = isPrimaryHorizontal ? exitRow : exitCol;
                    int maxPos = isPrimaryHorizontal ? boardHeight - 1 : boardWidth - 1;
                    exitPositionSpinner.setValueFactory(
                        new SpinnerValueFactory.IntegerSpinnerValueFactory(0, maxPos, position));
                    
                    // Rebuild color dropdown with available vehicles
                    colorComboBox.getItems().clear();
                    colorComboBox.getItems().add("Primary (Red)");
                    for (char c : currentState.cars.keySet()) {
                        if (c != 'P') {
                            colorComboBox.getItems().add("Vehicle " + c);
                        }
                    }
                    colorComboBox.getSelectionModel().selectFirst();
                    currentColor = 'P';
                    
                    // Reset solution components
                    solutionStates.clear();
                    currentStepIndex = 0;
                    lblStep.setText("Step: 0/0");
                    btnPrev.setDisable(true);
                    btnPlay.setDisable(true);
                    btnNext.setDisable(true);
                    btnSaveResultToText.setDisable(true);
                    
                    // Reset metrics
                    lblVisitedNodes.setText("Nodes Visited: 0");
                    lblExecutionTime.setText("Execution Time: 0.000 seconds");
                    
                    // Initialize board with loaded state
                    initializeBoard();
                    updateColorLegend(colorLegend);
                    
                    // Show success message
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("File Loaded");
                    alert.setHeaderText("Puzzle configuration loaded");
                    alert.setContentText("The puzzle has been loaded from " + file.getName());
                    alert.showAndWait();
                    
                } catch (IOException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Load Error");
                    alert.setHeaderText("Could not load puzzle");
                    alert.setContentText("Error: " + ex.getMessage());
                    alert.showAndWait();
                }
            }
        });
        
        // Save button
        btnSaveResultToText.setOnAction(e -> {
            if (solutionStates.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Solution");
                alert.setHeaderText("No solution to save");
                alert.setContentText("Please solve the puzzle first.");
                alert.showAndWait();
                return;
            }
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Solution");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
            );
            
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    // Write solution information
                    writer.write("Rush Hour Puzzle Solution\n");
                    writer.write("=========================\n\n");
                    writer.write(String.format("Board size: %d x %d\n", boardWidth, boardHeight));
                    writer.write(String.format("Algorithm used: %s\n", selectedAlgorithm));
                    if (!selectedAlgorithm.equals("Uniform Cost Search (UCS)")) {
                        writer.write(String.format("Heuristic used: %s\n", selectedHeuristic.getName()));
                    }
                    writer.write(String.format("Total steps: %d\n\n", solutionStates.size() - 1));
                    
                    // Write move history
                    writer.write("Solution moves:\n");
                    
                    State lastState = solutionStates.get(solutionStates.size() - 1);
                    List<String> moves = lastState.getMoveHistory();
                    for (int i = 0; i < moves.size(); i++) {
                        writer.write(String.format("%2d. %s\n", i + 1, moves.get(i)));
                    }
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Save Successful");
                    alert.setHeaderText("Solution saved");
                    alert.setContentText("The solution has been saved to " + file.getAbsolutePath());
                    alert.showAndWait();
                    
                } catch (IOException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Save Error");
                    alert.setHeaderText("Could not save solution");
                    alert.setContentText("Error: " + ex.getMessage());
                    alert.showAndWait();
                }
            }
        });
        
        // Initialize the board
        initializeBoard();
        updateColorLegend(colorLegend);
        
        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
    
    private void initializeColorMap() {
        // Setup fixed colors for pieces
        colorMap.put('P', Color.RED);
        colorMap.put('K', Color.BLACK);
        
        // Regular pieces A-Z
        colorMap.put('A', Color.GREEN);
        colorMap.put('B', Color.BLUE);
        colorMap.put('C', Color.YELLOW);
        colorMap.put('D', Color.MAGENTA);
        colorMap.put('E', Color.CYAN);
        colorMap.put('F', Color.ORANGERED);
        colorMap.put('G', Color.DARKGREEN);
        colorMap.put('H', Color.DARKBLUE);
        colorMap.put('I', Color.GOLD);
        colorMap.put('J', Color.PURPLE);
        colorMap.put('K', Color.DARKTURQUOISE);
        colorMap.put('L', Color.DARKORANGE);
        
        // Generate additional colors if needed
        for (char c = 'M'; c <= 'Z'; c++) {
            double hue = ((c - 'M') / (double)('Z' - 'M')) * 360;
            Color color = Color.hsb(hue, 0.8, 0.9);
            colorMap.put(c, color);
        }
    }
    
    private void initializeEmptyState() {
        Map<Character, Car> cars = new HashMap<>();
        
        int totalBits = boardWidth * boardHeight;
        int chunkCount = (totalBits + 63) / 64;
        
        // Create primary car
        long[] primaryBitmask = new long[chunkCount];
        
        if (isPrimaryHorizontal) {
            // Place a 2-cell horizontal primary car in the middle row
            int row = boardHeight / 2;
            int col = 1;  // Start near the left
            
            primaryBitmask[0] |= (1L << (row * boardWidth + col));
            primaryBitmask[0] |= (1L << (row * boardWidth + col + 1));
            
            cars.put('P', new Car('P', true, 2, primaryBitmask, -1, row));
        } else {
            // Place a 2-cell vertical primary car in the middle column
            int col = boardWidth / 2;
            int row = 1;  // Start near the top
            
            primaryBitmask[0] |= (1L << (row * boardWidth + col));
            primaryBitmask[0] |= (1L << ((row + 1) * boardWidth + col));
            
            cars.put('P', new Car('P', false, 2, primaryBitmask, col, -1));
        }
        
        currentState = new State(cars, null, "", 0);
    }
    
    private void initializeBoard() {
        boardPane.getChildren().clear();
        boardButtons = new Button[boardHeight][boardWidth];
        
        // Calculate button size based on board dimensions
        double buttonSize = calculateButtonSize();
        
        for (int row = 0; row < boardHeight; row++) {
            for (int col = 0; col < boardWidth; col++) {
                Button button = new Button();
                button.setMinSize(buttonSize, buttonSize);
                button.setPrefSize(buttonSize, buttonSize);
                button.setMaxSize(buttonSize, buttonSize);
                
                // Style for empty cell
                button.setStyle("-fx-background-color: white; -fx-border-color: lightgray;");
                
                // Special style for exit position
                if (row == exitRow && col == exitCol) {
                    button.setStyle("-fx-background-color: black; -fx-text-fill: white;");
                    button.setText("K");
                }
                
                // Store row and column in the button properties
                final int r = row;
                final int c = col;
                
                // Add click handler to place/remove cars
                button.setOnAction(event -> {
                    if (currentColor == 'P') {
                        // For primary car, we handle it specially
                        placePrimaryCar(r, c);
                    } else if (currentState.cars.containsKey(currentColor)) {
                        // Remove existing car if clicked on it
                        removeCar(currentColor);
                        initializeBoard(); // Redraw the board
                    } else {
                        // Otherwise place a new car
                        placeNewCar(currentColor, r, c);
                    }
                });
                
                boardButtons[row][col] = button;
                boardPane.add(button, col, row);
            }
        }
        
        // Draw existing cars
        updateBoardFromState();
    }
    
    private void updateBoardFromState() {
        // Reset board
        for (int row = 0; row < boardHeight; row++) {
            for (int col = 0; col < boardWidth; col++) {
                Button button = boardButtons[row][col];
                button.setText("");
                button.setStyle("-fx-background-color: white; -fx-border-color: lightgray;");
                
                // Mark exit position
                if (row == exitRow && col == exitCol) {
                    button.setStyle("-fx-background-color: black; -fx-text-fill: white;");
                    button.setText("K");
                }
            }
        }
        
        // Draw all cars
        for (Car car : currentState.cars.values()) {
            Color color = colorMap.getOrDefault(car.id, Color.GRAY);
            String colorHex = String.format("#%02X%02X%02X", 
                (int)(color.getRed() * 255), 
                (int)(color.getGreen() * 255), 
                (int)(color.getBlue() * 255));
            
            // Find all occupied cells for this car
            for (int r = 0; r < boardHeight; r++) {
                for (int c = 0; c < boardWidth; c++) {
                    int idx = r * boardWidth + c;
                    int chunk = idx / 64;
                    int bit = idx % 64;
                    
                    if (chunk < car.bitmask.length && (car.bitmask[chunk] & (1L << bit)) != 0) {
                        Button button = boardButtons[r][c];
                        button.setStyle("-fx-background-color: " + colorHex + "; -fx-border-color: darkgray;");
                        button.setText(String.valueOf(car.id));
                    }
                }
            }
        }
    }
    
    private void placePrimaryCar(int row, int col) {
        // Create a new primary car at the clicked position
        int totalBits = boardWidth * boardHeight;
        int chunkCount = (totalBits + 63) / 64;
        long[] bitmask = new long[chunkCount];
        
        if (isPrimaryHorizontal) {
            // Check if we have room for a 2-cell horizontal car
            if (col >= boardWidth - 1) return;
            
            // Create horizontal car
            bitmask[0] |= (1L << (row * boardWidth + col));
            bitmask[0] |= (1L << (row * boardWidth + col + 1));
            
            // Remove any existing primary car
            if (currentState.cars.containsKey('P')) {
                removeCar('P');
            }
            
            // Add new primary car
            Car primaryCar = new Car('P', true, 2, bitmask, -1, row);
            Map<Character, Car> newCars = new HashMap<>(currentState.cars);
            newCars.put('P', primaryCar);
            currentState = new State(newCars, null, "", 0);
        } else {
            // Check if we have room for a 2-cell vertical car
            if (row >= boardHeight - 1) return;
            
            // Create vertical car
            bitmask[0] |= (1L << (row * boardWidth + col));
            bitmask[0] |= (1L << ((row + 1) * boardWidth + col));
            
            // Remove any existing primary car
            if (currentState.cars.containsKey('P')) {
                removeCar('P');
            }
            
            // Add new primary car
            Car primaryCar = new Car('P', false, 2, bitmask, col, -1);
            Map<Character, Car> newCars = new HashMap<>(currentState.cars);
            newCars.put('P', primaryCar);
            currentState = new State(newCars, null, "", 0);
        }
        
        // Update board
        updateBoardFromState();
    }
    
    private void placeNewCar(char carId, int startRow, int startCol) {
        // Get orientation from the user
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Car Orientation");
        alert.setHeaderText("Choose car orientation");
        alert.setContentText("Select the orientation for vehicle " + carId);
        
        ButtonType horizontalButton = new ButtonType("Horizontal");
        ButtonType verticalButton = new ButtonType("Vertical");
        
        alert.getButtonTypes().setAll(horizontalButton, verticalButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (!result.isPresent()) return;
        
        boolean isHorizontal = result.get() == horizontalButton;
        
        // Get length from user
        TextInputDialog lengthDialog = new TextInputDialog("2");
        lengthDialog.setTitle("Car Length");
        lengthDialog.setHeaderText("Enter car length");
        lengthDialog.setContentText("Length (2-" + (isHorizontal ? boardWidth : boardHeight) + "):");
        
        Optional<String> lengthResult = lengthDialog.showAndWait();
        if (!lengthResult.isPresent()) return;
        
        int length;
        try {
            length = Integer.parseInt(lengthResult.get());
            if (length < 2) length = 2;
            if (isHorizontal && length > boardWidth) length = boardWidth;
            if (!isHorizontal && length > boardHeight) length = boardHeight;
        } catch (NumberFormatException e) {
            length = 2; // Default
        }
        
        // Check bounds
        if (isHorizontal && startCol + length > boardWidth) return;
        if (!isHorizontal && startRow + length > boardHeight) return;
        
        // Create the car
        int totalBits = boardWidth * boardHeight;
        int chunkCount = (totalBits + 63) / 64;
        long[] bitmask = new long[chunkCount];
        
        for (int i = 0; i < length; i++) {
            if (isHorizontal) {
                bitmask[0] |= (1L << (startRow * boardWidth + (startCol + i)));
            } else {
                bitmask[0] |= (1L << ((startRow + i) * boardWidth + startCol));
            }
        }
        
        // Check for collisions with existing cars
        long[] occupied = State.buildOccupiedMask(currentState.cars, boardWidth, boardHeight);
        for (int i = 0; i < chunkCount; i++) {
            if ((bitmask[i] & occupied[i]) != 0) {
                // Collision detected
                Alert collisionAlert = new Alert(Alert.AlertType.ERROR);
                collisionAlert.setTitle("Collision");
                collisionAlert.setHeaderText("Cannot place car");
                collisionAlert.setContentText("The car would overlap with an existing car.");
                collisionAlert.showAndWait();
                return;
            }
        }
        
        // Add the car
        Car newCar = new Car(carId, isHorizontal, length, bitmask, 
                             isHorizontal ? -1 : startCol,
                             isHorizontal ? startRow : -1);
        
        Map<Character, Car> newCars = new HashMap<>(currentState.cars);
        newCars.put(carId, newCar);
        currentState = new State(newCars, null, "", 0);
        
        // Update board
        updateBoardFromState();
    }
    
    private void removeCar(char carId) {
        Map<Character, Car> newCars = new HashMap<>(currentState.cars);
        newCars.remove(carId);
        currentState = new State(newCars, null, "", 0);
    }
    
    // Calculate button size based on board dimensions
    private double calculateButtonSize() {
        int largerDimension = Math.max(boardWidth, boardHeight);
        
        // Scale down as board gets larger
        if (largerDimension <= 8) {
            return 50.0;  // Larger buttons for small boards
        } else if (largerDimension <= 12) {
            return 40.0;  // Medium size
        } else if (largerDimension <= 16) {
            return 30.0;  // Smaller
        } else {
            return 25.0;  // Very small for large boards
        }
    }
    
    private void updateColorLegend(FlowPane legendPane) {
        legendPane.getChildren().clear();
        
        // Add legend for primary car
        addLegendItem(legendPane, 'P', "Primary");
        
        // Add legend for exit
        addLegendItem(legendPane, 'K', "Exit");
        
        // Add legend for other cars
        for (char c : currentState.cars.keySet()) {
            if (c != 'P') {
                addLegendItem(legendPane, c, "Car " + c);
            }
        }
    }
    
    private void addLegendItem(FlowPane legendPane, char id, String label) {
        Color color = colorMap.getOrDefault(id, Color.GRAY);
        
        HBox legendItem = new HBox(5);
        legendItem.setAlignment(Pos.CENTER_LEFT);
        
        Rectangle colorBox = new Rectangle(15, 15, color);
        Label nameLabel = new Label(label);
        
        legendItem.getChildren().addAll(colorBox, nameLabel);
        legendPane.getChildren().add(legendItem);
    }
    
    private List<State> buildSolutionPath(State goalState) {
        List<State> path = new ArrayList<>();
        State current = goalState;
        
        // Traverse up to the root state
        while (current != null) {
            path.add(0, current); // Add to the beginning of the list
            current = current.parent;
        }
        
        return path;
    }
    
    private void showSolutionStep(int stepIndex) {
        if (stepIndex < 0 || stepIndex >= solutionStates.size()) {
            return;
        }
        
        currentState = solutionStates.get(stepIndex);
        updateBoardFromState();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}