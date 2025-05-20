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
    private char currentColor = 'P';
    private Button[][] boardButtons;
    private Map<Character, Color> colorMap = new HashMap<>();
    private boolean isPrimaryHorizontal = true;
    private int exitRow = 2;
    private int exitCol = 5; 
    private String exitDirection = "right"; // "right", "left", "top", "bottom"
    private List<State> solutionStates = new ArrayList<>();
    private int currentStepIndex = 0;
    private Timeline animationTimeline;
    private State currentState;
    private String selectedAlgorithm = "A*";
    private Heuristic selectedHeuristic;
    private Label lblVisitedNodes;
    private Label lblExecutionTime;
    private Label lblStep;
    private Button btnPrev;
    private Button btnPlay;
    private Button btnNext;
    private Button btnSaveResultToText;
    
    // Add fields to track car placement
    private boolean isPlacingCar = false;
    private List<Point> carPlacementPoints = new ArrayList<>();
    private Button instructionLabel;
    
    // Simple Point class to track grid coordinates
    private static class Point {
        int row, col;
        Point(int row, int col) {
            this.row = row;
            this.col = col;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Point)) return false;
            Point other = (Point) obj;
            return this.row == other.row && this.col == other.col;
        }
    }
    
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
        root.setAlignment(Pos.CENTER);
        
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
        
        // Exit side configuration - replacing exit position spinner
        VBox exitSideConfig = new VBox(5);
        exitSideConfig.setAlignment(Pos.CENTER_LEFT);
        Label lblExitSide = new Label("Exit Side:");
        
        // Toggle group for horizontal exit sides (initially visible)
        ToggleGroup exitHorizontalGroup = new ToggleGroup();
        HBox horizontalExitOptions = new HBox(10);
        horizontalExitOptions.setAlignment(Pos.CENTER);
        RadioButton rbRight = new RadioButton("Right");
        rbRight.setToggleGroup(exitHorizontalGroup);
        rbRight.setSelected(true); // Default to right
        RadioButton rbLeft = new RadioButton("Left");
        rbLeft.setToggleGroup(exitHorizontalGroup);
        horizontalExitOptions.getChildren().addAll(rbRight, rbLeft);
        
        // Toggle group for vertical exit sides (initially hidden)
        ToggleGroup exitVerticalGroup = new ToggleGroup();
        HBox verticalExitOptions = new HBox(10);
        verticalExitOptions.setAlignment(Pos.CENTER);
        RadioButton rbBottom = new RadioButton("Bottom");
        rbBottom.setToggleGroup(exitVerticalGroup);
        rbBottom.setSelected(true); // Default to bottom
        RadioButton rbTop = new RadioButton("Top");
        rbTop.setToggleGroup(exitVerticalGroup);
        verticalExitOptions.getChildren().addAll(rbBottom, rbTop);
        
        // Initially show horizontal options
        exitSideConfig.getChildren().addAll(lblExitSide, horizontalExitOptions);
        
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
        colorComboBox.getSelectionModel().selectFirst();
        colorSelection.getChildren().addAll(lblColor, colorComboBox);
        
        // Add button to add new cars to the board
        Button btnAddCar = new Button("Add New Car");
        btnAddCar.setMaxWidth(Double.MAX_VALUE);
        btnAddCar.setOnAction(e -> {
            // Find next available car ID (letter)
            char nextCarId = findNextAvailableCarId();
            
            // Don't proceed if we're out of letters
            if (nextCarId > 'Z') {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Too Many Cars");
                alert.setHeaderText("Maximum number of cars reached");
                alert.setContentText("You can only have up to 26 non-primary cars (A-Z).");
                alert.showAndWait();
                return;
            }
            
            // Add the new car to the state (with empty position for now)
            Map<Character, Car> newCars = new HashMap<>(currentState.cars);
            // Note: We're not adding an actual car yet, just reserving the ID
            
            // Rebuild the dropdown with sorted vehicles
            colorComboBox.getItems().clear();
            colorComboBox.getItems().add("Primary (Red)");
            
            // Get all existing car IDs plus the new one, sorted alphabetically
            List<Character> carIds = new ArrayList<>(currentState.cars.keySet());
            if (!carIds.contains(nextCarId)) {
                carIds.add(nextCarId);
            }
            Collections.sort(carIds);
            
            // Add all non-primary cars to the dropdown
            for (char carId : carIds) {
                if (carId != 'P') {
                    colorComboBox.getItems().add("Vehicle " + carId);
                }
            }
            
            // Select the new car
            colorComboBox.getSelectionModel().select("Vehicle " + nextCarId);
            
            // Explicitly set the current color
            currentColor = nextCarId;
            updateCarPlacementMode();
        });
        
        // Add the new car button right after the color selection dropdown
        colorSelection.getChildren().add(btnAddCar);

        // Create legend panel to show colors
        Label legendTitle = new Label("Color Legend:");
        FlowPane colorLegend = new FlowPane();
        colorLegend.setHgap(5);
        colorLegend.setVgap(5);
        colorLegend.setAlignment(Pos.CENTER);
        colorLegend.setPrefWrapLength(200);
        
        // Add exit direction info to legend
        Label exitInfoLabel = new Label("Exit: ");
        Label exitDirectionLabel = new Label("Right side");
        exitDirectionLabel.setStyle("-fx-font-weight: bold;");
        HBox exitInfo = new HBox(5, exitInfoLabel, exitDirectionLabel);
        exitInfo.setAlignment(Pos.CENTER_LEFT);
        
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
            exitSideConfig,
            btnApplyConfig
        );
        
        // Add editor and controls to the right panel
        rightPanel.getChildren().addAll(
            lblBoardEditor,
            colorSelection,
            legendTitle,
            colorLegend,
            exitInfo,
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
        selectedHeuristic = new Distance();
        initializeEmptyState();
        
        // Exit side handlers
        rbRight.setOnAction(e -> {
            exitDirection = "right";
            exitDirectionLabel.setText("Right side");
            exitCol = boardWidth - 1;
            updateBoardFromState();
        });
        
        rbLeft.setOnAction(e -> {
            exitDirection = "left";
            exitDirectionLabel.setText("Left side");
            exitCol = 0;
            updateBoardFromState();
        });
        
        rbBottom.setOnAction(e -> {
            exitDirection = "bottom";
            exitDirectionLabel.setText("Bottom side");
            exitRow = boardHeight - 1;
            updateBoardFromState();
        });
        
        rbTop.setOnAction(e -> {
            exitDirection = "top";
            exitDirectionLabel.setText("Top side");
            exitRow = 0;
            updateBoardFromState();
        });
        
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
            
            // Set exit gate position and direction based on orientation and selected radio buttons
            if (isPrimaryHorizontal) {
                if (rbRight.isSelected()) {
                    exitDirection = "right";
                    exitDirectionLabel.setText("Right side");
                    exitCol = boardWidth - 1;
                } else {
                    exitDirection = "left";
                    exitDirectionLabel.setText("Left side");
                    exitCol = 0;
                }
                // Initialize exit row to middle of board for horizontal car
                exitRow = boardHeight / 2;
            } else {
                if (rbBottom.isSelected()) {
                    exitDirection = "bottom";
                    exitDirectionLabel.setText("Bottom side");
                    exitRow = boardHeight - 1;
                } else {
                    exitDirection = "top";
                    exitDirectionLabel.setText("Top side");
                    exitRow = 0;
                }
                // Initialize exit col to middle of board for vertical car
                exitCol = boardWidth / 2;
            }
            
            // Rebuild color dropdown with available vehicles
            colorComboBox.getItems().clear();
            colorComboBox.getItems().add("Primary (Red)");
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
            
            // Switch exit options to horizontal
            exitSideConfig.getChildren().clear();
            exitSideConfig.getChildren().addAll(lblExitSide, horizontalExitOptions);
            
            // Set default exit direction
            if (rbRight.isSelected()) {
                exitDirection = "right";
                exitDirectionLabel.setText("Right side");
                exitCol = boardWidth - 1;
            } else {
                exitDirection = "left";
                exitDirectionLabel.setText("Left side");
                exitCol = 0;
            }
            
            // Default exit row to middle of board
            exitRow = boardHeight / 2;
            
            initializeEmptyState();
            initializeBoard();
        });
        
        rbVertical.setOnAction(e -> {
            isPrimaryHorizontal = false;
            
            // Switch exit options to vertical
            exitSideConfig.getChildren().clear();
            exitSideConfig.getChildren().addAll(lblExitSide, verticalExitOptions);
            
            // Set default exit direction
            if (rbBottom.isSelected()) {
                exitDirection = "bottom";
                exitDirectionLabel.setText("Bottom side");
                exitRow = boardHeight - 1;
            } else {
                exitDirection = "top";
                exitDirectionLabel.setText("Top side");
                exitRow = 0;
            }
            
            // Default exit col to middle of board
            exitCol = boardWidth / 2;
            
            initializeEmptyState();
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
                updateCarPlacementMode();
            }
        });
        
        // Add a "Finish Car" button
        Button btnFinishCar = new Button("Finish Car Placement");
        btnFinishCar.setMaxWidth(Double.MAX_VALUE);
        btnFinishCar.setOnAction(e -> finishCarPlacement());
        
        // Add before the color legend in the right panel
        rightPanel.getChildren().add(rightPanel.getChildren().indexOf(colorLegend), btnFinishCar);
        
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
                        visitedNodes = astar.getVisitedNodeCount();
                        break;
                        
                    case "Uniform Cost Search (UCS)":
                        UCS ucs = new UCS(boardWidth, boardHeight, exitRow, exitCol, exitDirection);
                        solution = ucs.find(currentState);
                        visitedNodes = ucs.getVisitedNodeCount();
                        break;
                        
                    case "Greedy Best-First Search":
                        GreedyBFS greedy = new GreedyBFS(boardWidth, boardHeight, exitRow, exitCol, exitDirection, selectedHeuristic);
                        solution = greedy.find(currentState);
                        visitedNodes = greedy.getVisitedNodeCount();
                        break;
                        
                    case "Iterative Deepening A*":
                        IDAStar ida = new IDAStar(boardWidth, boardHeight, exitRow, exitCol, exitDirection, selectedHeuristic);
                        solution = ida.find(currentState);
                        visitedNodes = ida.getVisitedNodeCount();
                        break;
                        
                    default:
                        throw new IllegalStateException("Unknown algorithm selected: " + selectedAlgorithm);
                }
                
                long endTime = System.currentTimeMillis();
                double executionTime = (endTime - startTime) / 1000.0;
                
                // Update metrics
                lblExecutionTime.setText(String.format("Execution Time: %.3f seconds", executionTime));
                lblVisitedNodes.setText("Nodes Visited: " + visitedNodes);
                
                if (solution != null) {
                    // Build solution states list by traversing the solution path
                    solutionStates = buildSolutionPath(solution);
                    
                    // Update UI
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
                        
                        // Update radio buttons for car orientation
                        if (isPrimaryHorizontal) {
                            rbHorizontal.setSelected(true);
                            
                            // Switch exit options to horizontal
                            exitSideConfig.getChildren().clear();
                            exitSideConfig.getChildren().addAll(lblExitSide, horizontalExitOptions);
                            
                            // Set correct exit direction radio button
                            if ("right".equals(exitDirection)) {
                                rbRight.setSelected(true);
                                exitDirectionLabel.setText("Right side");
                            } else {
                                rbLeft.setSelected(true);
                                exitDirectionLabel.setText("Left side");
                            }
                        } else {
                            rbVertical.setSelected(true);
                            

                            // Switch exit options to vertical
                            exitSideConfig.getChildren().clear();
                            exitSideConfig.getChildren().addAll(lblExitSide, verticalExitOptions);
                            
                            // Set correct exit direction radio button
                            if ("bottom".equals(exitDirection)) {
                                rbBottom.setSelected(true);
                                exitDirectionLabel.setText("Bottom side");
                            } else {
                                rbTop.setSelected(true);
                                exitDirectionLabel.setText("Top side");
                            }
                        }
                    }
                    
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
                    writer.write(String.format("Exit direction: %s\n", exitDirection.toUpperCase()));
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
                
                // Style for empty cell (no green indicator)
                button.setStyle("-fx-background-color: white; -fx-border-color: lightgray;");
                
                // Store row and column in the button properties
                final int r = row;
                final int c = col;
                
                // Add click handler for car placement
                button.setOnAction(event -> handleGridCellClick(r, c));
                
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
        
        // If in car placement mode, highlight current selected cells
        if (isPlacingCar && !carPlacementPoints.isEmpty()) {
            Color color = colorMap.getOrDefault(currentColor, Color.GRAY);
            String colorHex = String.format("#%02X%02X%02X", 
                (int)(color.getRed() * 255), 
                (int)(color.getGreen() * 255), 
                (int)(color.getBlue() * 255));
            
            for (Point p : carPlacementPoints) {
                if (p.row >= 0 && p.row < boardHeight && p.col >= 0 && p.col < boardWidth) {
                    Button button = boardButtons[p.row][p.col];
                    button.setStyle("-fx-background-color: " + colorHex + "; -fx-border-color: red; -fx-border-width: 2px;");
                    button.setText(String.valueOf(currentColor));
                }
            }
        }
    }
    
    // Handle grid cell clicks for car placement
    private void handleGridCellClick(int row, int col) {
        if (currentState.cars.containsKey(currentColor)) {
            // If the car already exists, clicking on it should remove it
            if (isCarAtPosition(currentColor, row, col)) {
                removeCar(currentColor);
                carPlacementPoints.clear();
                isPlacingCar = false;
                instructionLabel.setText("Click to place cars");
                updateBoardFromState();
                return;
            }
        }
        
        // Start placing a new car
        Point clickedPoint = new Point(row, col);
        
        if (!isPlacingCar) {
            // First click - start car placement
            isPlacingCar = true;
            carPlacementPoints.clear();
            carPlacementPoints.add(clickedPoint);
            instructionLabel.setText("Continue clicking to extend car (press Enter when done)");
        } else {
            // Check if the new point is valid (straight line)
            if (carPlacementPoints.contains(clickedPoint)) {
                // Clicked on an already selected cell - ignore
                return;
            }
            
            if (!isValidCarExtension(clickedPoint)) {
                // Not a valid extension - show error
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Car Shape");
                alert.setHeaderText("Cars must be straight lines");
                alert.setContentText("Please place car cells in a straight horizontal or vertical line");
                alert.showAndWait();
                return;
            }
            
            // Valid extension - add the point
            carPlacementPoints.add(clickedPoint);
        }
        
        // Update the board to show the car being placed
        updateBoardFromState();
        
        // If this is the second point, check if we can determine orientation
        if (carPlacementPoints.size() == 2) {
            // Create a key event handler for Enter key to finish car placement
            Scene scene = boardPane.getScene();
            scene.setOnKeyPressed(e -> {
                switch (e.getCode()) {
                    case ENTER:
                        finishCarPlacement();
                        scene.setOnKeyPressed(null); // Remove the handler
                        break;
                    default:
                        break;
                }
            });
        }
    }
    
    private boolean isCarAtPosition(char carId, int row, int col) {
        Car car = currentState.cars.get(carId);
        if (car == null) return false;
        
        int idx = row * boardWidth + col;
        int chunk = idx / 64;
        int bit = idx % 64;
        
        return chunk < car.bitmask.length && (car.bitmask[chunk] & (1L << bit)) != 0;
    }
    
    private boolean isValidCarExtension(Point newPoint) {
        if (carPlacementPoints.isEmpty()) return true;
        
        // If this is the first extension, it's valid
        if (carPlacementPoints.size() == 1) {
            Point first = carPlacementPoints.get(0);
            // Must be adjacent horizontally or vertically
            return (first.row == newPoint.row && Math.abs(first.col - newPoint.col) == 1) ||
                   (first.col == newPoint.col && Math.abs(first.row - newPoint.row) == 1);
        }
        
        // We already have at least 2 points, so we know the orientation
        Point first = carPlacementPoints.get(0);
        Point second = carPlacementPoints.get(1);
        
        boolean isHorizontal = first.row == second.row;
        
        if (isHorizontal) {
            // Must be in the same row as existing points
            if (newPoint.row != first.row) return false;
            
            // Find min/max column so far
            int minCol = Integer.MAX_VALUE;
            int maxCol = Integer.MIN_VALUE;
            for (Point p : carPlacementPoints) {
                minCol = Math.min(minCol, p.col);
                maxCol = Math.max(maxCol, p.col);
            }
            
            // New column must be adjacent to min or max
            return newPoint.col == minCol - 1 || newPoint.col == maxCol + 1;
        } else {
            // Must be in the same column as existing points
            if (newPoint.col != first.col) return false;
            
            // Find min/max row so far
            int minRow = Integer.MAX_VALUE;
            int maxRow = Integer.MIN_VALUE;
            for (Point p : carPlacementPoints) {
                minRow = Math.min(minRow, p.row);
                maxRow = Math.max(maxRow, p.row);
            }
            
            // New row must be adjacent to min or max
            return newPoint.row == minRow - 1 || newPoint.row == maxRow + 1;
        }
    }
    
    private void finishCarPlacement() {
        if (carPlacementPoints.size() < 2) {
            // Need at least 2 cells to make a car
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Car Size");
            alert.setHeaderText("Car is too small");
            alert.setContentText("Cars must be at least 2 cells long");
            alert.showAndWait();
            return;
        }
        
        // Determine orientation
        Point first = carPlacementPoints.get(0);
        Point second = carPlacementPoints.get(1);
        boolean isHorizontal = first.row == second.row;
        
        // Create bitmask for the car
        int totalBits = boardWidth * boardHeight;
        int chunkCount = (totalBits + 63) / 64;
        long[] bitmask = new long[chunkCount];
        
        for (Point p : carPlacementPoints) {
            int idx = p.row * boardWidth + p.col;
            bitmask[idx / 64] |= (1L << (idx % 64));
        }
        
        // Check for collisions with existing cars
        long[] occupied = State.buildOccupiedMask(currentState.cars, boardWidth, boardHeight);
        for (int i = 0; i < chunkCount; i++) {
            if ((bitmask[i] & occupied[i]) != 0) {
                // Collision detected
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Collision");
                alert.setHeaderText("Cannot place car");
                alert.setContentText("The car would overlap with an existing car.");
                alert.showAndWait();
                carPlacementPoints.clear();
                isPlacingCar = false;
                updateBoardFromState();
                return;
            }
        }
        
        // Create the car
        int row = isHorizontal ? first.row : -1;
        int col = isHorizontal ? -1 : first.col;
        Car newCar = new Car(currentColor, isHorizontal, carPlacementPoints.size(), bitmask, col, row);
        
        // Add to the state
        Map<Character, Car> newCars = new HashMap<>(currentState.cars);
        newCars.put(currentColor, newCar);
        currentState = new State(newCars, null, "", 0);
        
        // Reset placement mode
        carPlacementPoints.clear();
        isPlacingCar = false;
        instructionLabel.setText("Click to place cars");
        
        // Update board
        updateBoardFromState();
    }
    
    // Replace the old placeNewCar method
    private void placeNewCar(char carId, int startRow, int startCol) {
        // Now handled by the interactive car placement system
        handleGridCellClick(startRow, startCol);
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

        /**
     * Resets the car placement mode when selecting a different car
     */
    private void updateCarPlacementMode() {
        // Reset placement state when changing selected car
        carPlacementPoints.clear();
        isPlacingCar = false;
        
        // Only update the instruction text if it's been initialized
        if (instructionLabel != null) {
            instructionLabel.setText("Click to place cars");
        }
        
        updateBoardFromState();
    }
    
    /**
     * Removes a car with the specified ID from the current state
     * 
     * @param carId The ID of the car to remove
     */
    private void removeCar(char carId) {
        Map<Character, Car> newCars = new HashMap<>(currentState.cars);
        newCars.remove(carId);
        currentState = new State(newCars, null, "", 0);
        
        // Update board display
        updateBoardFromState();
    }
    
    /**
     * Finds the next available letter ID (A-Z) for a new car
     * 
     * @return Next available car ID, or character beyond 'Z' if all are taken
     */
    private char findNextAvailableCarId() {
        // Start from 'A' and find the first letter not already used
        for (char c = 'A'; c <= 'Z'; c++) {
            if (!currentState.cars.containsKey(c)) {
                return c;
            }
        }
        // If we get here, all letters A-Z are taken
        return '[';  // Return a character beyond 'Z' to indicate no more IDs available
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}