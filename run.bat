@echo off
REM Build the project
call gradlew shadowJar

REM Create bin directory if it doesn't exist
if not exist bin mkdir bin

REM Check if jar exists and copy it if needed
if exist build\libs\RushHourSolver.jar (
    if not exist bin\RushHourSolver.jar (
        copy build\libs\RushHourSolver.jar bin\
    )
)

REM Run the jar
if exist bin\RushHourSolver.jar (
    java -jar bin\RushHourSolver.jar %*
) else (
    echo Error: JAR file not created. Check the build logs.
    exit /b 1
)