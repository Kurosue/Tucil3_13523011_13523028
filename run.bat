javac --module-path "C:\javafx-sdk-23.0.2\lib" --add-modules javafx.controls,javafx.fxml -d bin src\.java src\util\.java src\pathfinding\.java src\heuristic\.java
@echo off
if "%1"=="" (
    java --module-path "C:\javafx-sdk-23.0.2\lib" --add-modules javafx.controls,javafx.fxml -cp bin Main
) else if /I "%1"=="TEST" (
    if "%2"=="" (
        echo Usage: run.bat [TEST testfile]
    ) else (
        java --module-path "C:\javafx-sdk-23.0.2\lib" --add-modules javafx.controls,javafx.fxml -cp bin test/%2
    )
) else (
    echo Usage: run.bat [TEST testfile]
)