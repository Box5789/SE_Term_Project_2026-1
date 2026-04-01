#!/bin/bash
set -e

# Output directory
mkdir -p out

# Base classpath for compilation
CP="libs/jackson-databind-2.15.2.jar:libs/jackson-core-2.15.2.jar:libs/jackson-annotations-2.15.2.jar:libs/jackson-datatype-jsr310-2.15.2.jar:libs/junit-platform-console-standalone-1.9.3.jar:libs/flatlaf-3.2.jar:libs/flatlaf-extras-3.2.jar"

# Add JavaFX jars if they exist
if ls libs/javafx-*.jar >/dev/null 2>&1; then
  JAVA_FX_CP=$(echo libs/javafx-*.jar | tr ' ' ':')
  CP="$CP:$JAVA_FX_CP"
fi

echo "Compiling Main classes..."
find src/main/java -name "*.java" > sources.txt
javac -d out -cp "$CP" @sources.txt

# Copy resources
if [ -d "src/main/resources" ]; then
  cp -r src/main/resources/* out/
fi

echo "Compiling Test classes..."
find src/test/java -name "*.java" > test_sources.txt
javac -d out -cp "out:$CP" @test_sources.txt

echo "Running tests..."
java -jar libs/junit-platform-console-standalone-1.9.3.jar --class-path "out:$CP" --scan-class-path

# Cleanup
rm sources.txt test_sources.txt
