javac -d bin -sourcepath src src/*.java src/**/*.java

if [ $# -eq 0 ]; then
    java -cp bin Main
elif [ "$1" = "TEST" ] && [ $# -ge 2 ]; then
    java -cp bin Main src/test/$2
else
    echo "Usage: ./run [TEST testfile]"
fi