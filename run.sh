#!/bin/bash
set -e

# 1. 라이브러리 체크 및 설치
if [ ! -d "libs" ] || [ ! -f "libs/jackson-databind-2.15.2.jar" ]; then
    echo "Libraries not found. Running setup_libs.sh..."
    bash setup_libs.sh
fi

# 2. 컴파일 (out 폴더)
mkdir -p out
CP="libs/jackson-databind-2.15.2.jar:libs/jackson-core-2.15.2.jar:libs/jackson-annotations-2.15.2.jar:libs/jackson-datatype-jsr310-2.15.2.jar"

echo "Compiling..."
find src/main/java -name "*.java" > sources.txt
javac -d out -cp "$CP" @sources.txt
rm sources.txt

# 3. 실행
echo "Running IMS..."
# Headless 환경인지 확인하여 GUI 가능 여부 출력
if [ "$(uname)" == "Darwin" ]; then
    # MacOS의 경우 앱 런처를 통해 실행할 수도 있지만, 여기서는 커맨드라인에서 실행
    java -cp "out:$CP" com.se2026.ims.Main
else
    java -cp "out:$CP" com.se2026.ims.Main
fi
