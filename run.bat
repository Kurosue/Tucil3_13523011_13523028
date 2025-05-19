javac -d bin -sourcepath src src/MainCLI.java src/util/*.java src/test/*.java src/pathfinding/*.java

@echo off
if "%1"=="" (
    java -cp bin Main
) else if /I "%1"=="TEST" (
    if "%2"=="" (
        echo Usage: run.bat [TEST testfile]
    ) else (
        java -cp bin test/%2
    )
) else (
    echo Usage: run.bat [TEST testfile]
)