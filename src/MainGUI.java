import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;
import javafx.scene.paint.Color;
import javafx.collections.FXCollections;
import java.io.File;
import java.util.*;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

public class MainGUI extends Application {
    private GridPane boardPane;
    private int boardWidth = 6;
    private int boardHeight = 6;
    private int numPieces = 5;
    private char currentColor = 'A';
    private Button[][] boardButtons;
    private Map<Character, Color> colorMap = new HashMap<>();
    private Character primaryPieceOrientation = 'H'; // Horizontal by default
    private int exitGateRow = 2;  // Default exit position
    private int exitGateCol = 5;  // Default at right edge
    private List<char[][]> solutionSteps = new ArrayList<>();
    private int currentStepIndex = 0;
    private Timeline animationTimeline;
    
    // New fields for algorithm selection
    private String selectedAlgorithm = "A*";
    private String selectedHeuristic = "Distance to Exit";
    private Label lblVisitedNodes;
    private Label lblExecutionTime;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Rush Hour Puzzle Solver");
    
        // Initialize layout components
        boardPane = new GridPane();
        boardPane.setHgap(2);
        boardPane.setVgap(2);
        boardPane.setAlignment(Pos.CENTER);
        boardPane.setPadding(new Insets(10));
    
        lblVisitedNodes = new Label("Nodes Visited: ");
        lblExecutionTime = new Label("Execution Time: ");
    
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
        Spinner<Integer> widthSpinner = new Spinner<>(3, 40, boardWidth);
        widthSpinner.setEditable(true);
        widthSpinner.setPrefWidth(70);
        Label lblHeight = new Label("Height:");
        Spinner<Integer> heightSpinner = new Spinner<>(3, 40, boardHeight);
        heightSpinner.setEditable(true);
        heightSpinner.setPrefWidth(70);
        sizeConfig.getChildren().addAll(lblWidth, widthSpinner, lblHeight, heightSpinner);
    
        // Number of pieces configuration
        HBox piecesConfig = new HBox(10);
        piecesConfig.setAlignment(Pos.CENTER);
        Label lblPieces = new Label("Non-Primary Pieces:");
        Spinner<Integer> piecesSpinner = new Spinner<>(1, 25, numPieces);
        piecesSpinner.setEditable(true);
        piecesSpinner.setPrefWidth(70);
        piecesConfig.getChildren().addAll(lblPieces, piecesSpinner);
    
        // Algorithm selection
        Label lblSolverConfig = new Label("Solver Configuration");
        lblSolverConfig.setStyle("-fx-font-weight: bold; -fx-underline: true;");
        
        VBox algorithmConfig = new VBox(5);
        algorithmConfig.setAlignment(Pos.CENTER_LEFT);
        Label lblAlgorithm = new Label("Pathfinding Algorithm:");
        ComboBox<String> algorithmComboBox = new ComboBox<>();
        algorithmComboBox.setMaxWidth(Double.MAX_VALUE);
        algorithmComboBox.setItems(FXCollections.observableArrayList(
            "A*", "Greedy Best-First Search", "Uniform Cost Search (UCS)"
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
            "Distance to Exit", "Blocking Vehicles", "Combined"
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
        Label lblExitGate = new Label("Exit Gate Position:");
        Spinner<Integer> exitPositionSpinner = new Spinner<>(0, boardHeight-1, exitGateRow);
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
        TilePane colorLegend = new TilePane();
        colorLegend.setPrefColumns(3);
        colorLegend.setHgap(5);
        colorLegend.setVgap(5);
        colorLegend.setAlignment(Pos.CENTER);
        
        // Load and solve buttons
        Label lblOperations = new Label("Operations");
        lblOperations.setStyle("-fx-font-weight: bold; -fx-underline: true;");
        
        Button btnLoadFromFile = new Button("Load From File");
        btnLoadFromFile.setMaxWidth(Double.MAX_VALUE);
        
        Button btnSolve = new Button("Solve Puzzle");
        btnSolve.setMaxWidth(Double.MAX_VALUE);
        
        Button btnSaveResultToText = new Button("Save Solution to Text");
        btnSaveResultToText.setMaxWidth(Double.MAX_VALUE);
        btnSaveResultToText.setDisable(true);
        
        // Solution navigation controls
        Label lblSolution = new Label("Solution Navigation");
        lblSolution.setStyle("-fx-font-weight: bold; -fx-underline: true;");
        
        HBox navigationControls = new HBox(10);
        navigationControls.setAlignment(Pos.CENTER);
        Button btnPrev = new Button("◀");
        Button btnPlay = new Button("▶");
        Button btnNext = new Button("▶▶");
        navigationControls.getChildren().addAll(btnPrev, btnPlay, btnNext);
        
        Label lblStep = new Label("Step: 0/0");
        lblStep.setAlignment(Pos.CENTER);
        
        btnPrev.setDisable(true);
        btnPlay.setDisable(true);
        btnNext.setDisable(true);
    
        // Initialize color map
        initializeColorMap();
        
        // Add configuration components to the left panel
        leftPanel.getChildren().addAll(
            lblBoardConfig,
            sizeConfig, 
            piecesConfig,
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
        
        // Event handlers for apply config button
        btnApplyConfig.setOnAction(e -> {
            boardWidth = widthSpinner.getValue();
            boardHeight = heightSpinner.getValue();
            numPieces = piecesSpinner.getValue();
            selectedAlgorithm = algorithmComboBox.getValue();
            selectedHeuristic = heuristicComboBox.getValue();
            primaryPieceOrientation = rbHorizontal.isSelected() ? 'H' : 'V';
            
            // Update exit gate position spinner max value based on board dimensions
            int maxPos = primaryPieceOrientation == 'H' ? boardHeight-1 : boardWidth-1;
            exitPositionSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, maxPos, 
                    Math.min(exitPositionSpinner.getValue(), maxPos)));
            
            // Set exit gate position based on orientation
            exitGateRow = primaryPieceOrientation == 'H' ? 
                exitPositionSpinner.getValue() : boardHeight/2;
            exitGateCol = primaryPieceOrientation == 'V' ? 
                exitPositionSpinner.getValue() : boardWidth-1;
            
            // Rebuild color dropdown
            colorComboBox.getItems().clear();
            colorComboBox.getItems().add("Primary (Red)");
            for (int i = 0; i < numPieces; i++) {
                char colorChar = (char)('A' + i);
                colorComboBox.getItems().add("Piece " + colorChar);
            }
            colorComboBox.getSelectionModel().selectFirst();
            
            // Initialize the board
            initializeBoard();
            
            // Reset solution steps
            solutionSteps.clear();
            currentStepIndex = 0;
            lblStep.setText("Step: 0/0");
            btnPrev.setDisable(true);
            btnPlay.setDisable(true);
            btnNext.setDisable(true);
            
            // Reset metrics
            lblVisitedNodes.setText("Nodes Visited: ");
            lblExecutionTime.setText("Execution Time: ");
            
            // Update color legend
            updateColorLegend(colorLegend);
        });

        // Event listeners for orientation change
        rbHorizontal.setOnAction(e -> {
            int maxPos = boardHeight-1;
            exitPositionSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, maxPos, 
                    Math.min(exitPositionSpinner.getValue(), maxPos)));
            exitGateRow = exitPositionSpinner.getValue();
            exitGateCol = boardWidth-1;
            initializeBoard();
        });
        
        rbVertical.setOnAction(e -> {
            int maxPos = boardWidth-1;
            exitPositionSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, maxPos, 
                    Math.min(exitPositionSpinner.getValue(), maxPos)));
            exitGateRow = boardHeight-1;
            exitGateCol = exitPositionSpinner.getValue();
            initializeBoard();
        });
        
        // Exit position change listener
        exitPositionSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (primaryPieceOrientation == 'H') {
                exitGateRow = newVal;
            } else {
                exitGateCol = newVal;
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
                    // Extract the character from "Piece X"
                    currentColor = selected.charAt(selected.length() - 1);
                }
            }
        });

        // Solution navigation event handlers
        btnPrev.setOnAction(e -> {
            if (currentStepIndex > 0) {
                currentStepIndex--;
                showSolutionStep(currentStepIndex);
                lblStep.setText("Step: " + currentStepIndex + "/" + (solutionSteps.size()-1));
                btnNext.setDisable(false);
                if (currentStepIndex == 0) {
                    btnPrev.setDisable(true);
                }
            }
        });
        
        btnNext.setOnAction(e -> {
            if (currentStepIndex < solutionSteps.size() - 1) {
                currentStepIndex++;
                showSolutionStep(currentStepIndex);
                lblStep.setText("Step: " + currentStepIndex + "/" + (solutionSteps.size()-1));
                btnPrev.setDisable(false);
                if (currentStepIndex == solutionSteps.size() - 1) {
                    btnNext.setDisable(true);
                }
            }
        });
        
        btnPlay.setOnAction(e -> {
            if (animationTimeline != null && animationTimeline.getStatus() == Timeline.Status.RUNNING) {
                animationTimeline.stop();
                btnPlay.setText("▶");
            } else {
                animationTimeline = new Timeline();
                animationTimeline.setCycleCount(solutionSteps.size() - currentStepIndex - 1);
                
                KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), event -> {
                    if (currentStepIndex < solutionSteps.size() - 1) {
                        currentStepIndex++;
                        showSolutionStep(currentStepIndex);
                        lblStep.setText("Step: " + currentStepIndex + "/" + (solutionSteps.size()-1));
                        
                        if (currentStepIndex == solutionSteps.size() - 1) {
                            btnNext.setDisable(true);
                            btnPlay.setText("▶");
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
            // Extract board configuration
            char[][] boardConfig = getBoardConfiguration();
            
            // Measure execution time
            long startTime = System.currentTimeMillis();
            
            // Call the solver with selected algorithm and heuristic
            solutionSteps = solvePuzzle(boardConfig, selectedAlgorithm, selectedHeuristic);
            
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            // Update metrics
            lblExecutionTime.setText("Execution Time: " + executionTime + " ms");
            
            // For now we're simulating a successful solve
            int nodesVisited = 45; // This would come from the actual solver
            lblVisitedNodes.setText("Nodes Visited: " + nodesVisited);
            
            // Enable result saving
            btnSaveResultToText.setDisable(false);
            
            // Enable solution navigation if we have steps
            if (solutionSteps.size() > 1) {
                btnPrev.setDisable(true);
                btnPlay.setDisable(false);
                btnNext.setDisable(false);
                currentStepIndex = 0;
                lblStep.setText("Step: 0/" + (solutionSteps.size()-1));
                
                // Display first step (initial configuration)
                showSolutionStep(0);
            }
        });

        // File load button
        btnLoadFromFile.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Rush Hour Configuration File");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
            );
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                loadBoardFromFile(selectedFile);
            }
        });
        
        // Save button
        btnSaveResultToText.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Solution");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
            );
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                saveSolutionToFile(file);
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Saved");
                alert.setContentText("Solution saved to " + file.getName());
                alert.showAndWait();
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
        Color[] fixedColors = {
            Color.RED,         // Primary piece
            Color.GREEN,
            Color.BLUE,
            Color.YELLOW,
            Color.MAGENTA,
            Color.CYAN,
            Color.ORANGERED,
            Color.LIGHTGREEN,
            Color.LIGHTYELLOW,
            Color.LIGHTBLUE,
            Color.PURPLE,
            Color.LIGHTCYAN
        };
        
        // Primary piece (now using 'P' as specified)
        colorMap.put('P', Color.RED);
        
        // Regular pieces A-Z
        for (int i = 0; i < 12 && i < 26; i++) {
            char letter = (char) ('A' + i);
            colorMap.put(letter, fixedColors[i % fixedColors.length]);
        }
        
        // Generate additional colors if needed
        for (int i = 12; i < 26; i++) {
            char letter = (char) ('A' + i);
            double hue = (i * 360.0) / 26;
            Color generated = Color.hsb(hue, 0.8, 0.8);
            colorMap.put(letter, generated);
        }
        
        // Add exit gate color
        colorMap.put('K', Color.BLACK);
    }
    
    private void initializeBoard() {
        boardPane.getChildren().clear();
        boardButtons = new Button[boardHeight][boardWidth];
        
        // Calculate button size based on board dimensions
        double buttonSize = calculateButtonSize();
        
        for (int row = 0; row < boardHeight; row++) {
            for (int col = 0; col < boardWidth; col++) {
                Button btn = new Button(" ");
                btn.setMinSize(buttonSize, buttonSize);
                btn.setMaxSize(buttonSize, buttonSize);
                
                // Check if this cell is the exit gate
                boolean isExitGate = false;
                if (primaryPieceOrientation == 'H' && col == boardWidth-1 && row == exitGateRow) {
                    isExitGate = true;
                    btn.setText("K");
                    btn.setStyle("-fx-background-color: #000000; -fx-text-fill: white; -fx-border-color: black;");
                } else if (primaryPieceOrientation == 'V' && row == boardHeight-1 && col == exitGateCol) {
                    isExitGate = true;
                    btn.setText("K");
                    btn.setStyle("-fx-background-color: #000000; -fx-text-fill: white; -fx-border-color: black;");
                } else {
                    btn.setStyle("-fx-background-color: white; -fx-border-color: black;");
                }
                
                // Rest of your existing button setup code...
                
                // Store row and col in final variables for the lambda
                final int r = row;
                final int c = col;
                final boolean isExit = isExitGate;
                
                // Your existing event handlers...
                
                boardPane.add(btn, col, row);
                boardButtons[row][col] = btn;
            }
        }
    }
    
    // Add this new method to calculate button size
    private double calculateButtonSize() {
        int threshold = 18;
        double baseSize = 40.0;  // Default size for small boards
        
        // Find the larger dimension
        int largerDimension = Math.max(boardWidth, boardHeight);
        
        if (largerDimension <= threshold) {
            return baseSize; // Use default size if under threshold
        } else {
            // Scale down based on how much larger than threshold
            return Math.max(baseSize * threshold / largerDimension, 12.0); // Minimum size of 12px
        }
    }
    
    private char[][] getBoardConfiguration() {
        char[][] config = new char[boardHeight][boardWidth];
        
        for (int row = 0; row < boardHeight; row++) {
            for (int col = 0; col < boardWidth; col++) {
                String text = boardButtons[row][col].getText().trim();
                if (text.isEmpty()) {
                    config[row][col] = '.'; // Empty cell as per spec
                } else {
                    config[row][col] = text.charAt(0);
                }
            }
        }
        
        return config;
    }
    
    private void updateColorLegend(TilePane legendPane) {
        legendPane.getChildren().clear();
        
        // Primary piece
        Label primaryLabel = new Label("P - Primary");
        primaryLabel.setStyle(
            "-fx-background-color: #FF0000; " +
            "-fx-padding: 5px; " +
            "-fx-border-color: black;"
        );
        legendPane.getChildren().add(primaryLabel);
        
        // Exit gate
        Label exitLabel = new Label("K - Exit");
        exitLabel.setStyle(
            "-fx-background-color: #000000; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 5px; " +
            "-fx-border-color: black;"
        );
        legendPane.getChildren().add(exitLabel);
        
        // Other pieces
        for (int i = 0; i < numPieces; i++) {
            char colorChar = (char)('A' + i);
            Color pieceColor = colorMap.get(colorChar);
            String hex = String.format("#%02X%02X%02X",
                (int)(pieceColor.getRed()*255),
                (int)(pieceColor.getGreen()*255),
                (int)(pieceColor.getBlue()*255));
            
            Label label = new Label(colorChar + "");
            label.setStyle(
                "-fx-background-color: " + hex + "; " +
                "-fx-padding: 5px; " +
                "-fx-border-color: black;"
            );
            legendPane.getChildren().add(label);
        }
    }
    
    // This is just a stub that would call your actual solver
    private List<char[][]> solvePuzzle(char[][] boardConfig, String algorithm, String heuristic) {
        System.out.println("Solving puzzle using " + algorithm + " with " + heuristic + " heuristic");
        for (int row = 0; row < boardHeight; row++) {
            for (int col = 0; col < boardWidth; col++) {
                System.out.print(boardConfig[row][col] + " ");
            }
            System.out.println();
        }
        
        // For demonstration, create some sample solution steps
        List<char[][]> steps = new ArrayList<>();
        steps.add(boardConfig); // Initial state
        
        // Create a few dummy solution steps
        for (int i = 0; i < 5; i++) {
            char[][] newStep = new char[boardHeight][boardWidth];
            for (int r = 0; r < boardHeight; r++) {
                for (int c = 0; c < boardWidth; c++) {
                    newStep[r][c] = boardConfig[r][c];
                }
            }
            
            // Make some changes to simulate moves
            if (i % 2 == 0) {
                movePiece(newStep, 'A', true);
            } else {
                movePiece(newStep, 'P', true);
            }
            
            steps.add(newStep);
            boardConfig = newStep;
        }
        
        return steps;
    }
    
    // Simulate moving a piece for demo purposes
    private void movePiece(char[][] board, char pieceId, boolean moveRight) {
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[0].length; c++) {
                if (board[r][c] == pieceId) {
                    if (moveRight && c < board[0].length - 1 && board[r][c+1] == '.') {
                        board[r][c] = '.';
                        board[r][c+1] = pieceId;
                        return;
                    } else if (!moveRight && c > 0 && board[r][c-1] == '.') {
                        board[r][c] = '.';
                        board[r][c-1] = pieceId;
                        return;
                    }
                }
            }
        }
    }
    
    private void showSolutionStep(int stepIndex) {
        if (stepIndex < 0 || stepIndex >= solutionSteps.size() || solutionSteps.isEmpty()) {
            return;
        }
        
        char[][] step = solutionSteps.get(stepIndex);
        
        for (int row = 0; row < boardHeight; row++) {
            for (int col = 0; col < boardWidth; col++) {
                Button btn = boardButtons[row][col];
                char cell = step[row][col];
                
                if (cell == '.') {
                    btn.setText(" ");
                    btn.setStyle("-fx-background-color: white; -fx-border-color: black;");
                } else if (cell == 'K') {
                    btn.setText("K");
                    btn.setStyle("-fx-background-color: #000000; -fx-text-fill: white; -fx-border-color: black;");
                } else {
                    btn.setText(String.valueOf(cell));
                    Color pieceColor = colorMap.getOrDefault(cell, Color.GRAY);
                    String hex = String.format("#%02X%02X%02X",
                        (int)(pieceColor.getRed()*255),
                        (int)(pieceColor.getGreen()*255),
                        (int)(pieceColor.getBlue()*255));
                    btn.setStyle("-fx-background-color: " + hex + "; -fx-border-color: black;");
                }
            }
        }
    }
    
    // Placeholder for loading board from file
    private void loadBoardFromFile(File file) {
        // This would parse the file format as per specs
        System.out.println("Loading configuration from: " + file.getPath());
        // For now, just simulate a loaded board
        // In real implementation, parse according to spec
    }
    
    // Placeholder for saving solution to file
    private void saveSolutionToFile(File file) {
        System.out.println("Saving solution to: " + file.getPath());
    }

    public static void main(String[] args) {
        launch(args);
    }
}