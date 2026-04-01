#!/bin/bash
mkdir -p libs
curl -L -o libs/jackson-databind-2.15.2.jar https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.15.2/jackson-databind-2.15.2.jar
curl -L -o libs/jackson-core-2.15.2.jar https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.15.2/jackson-core-2.15.2.jar
curl -L -o libs/jackson-annotations-2.15.2.jar https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.15.2/jackson-annotations-2.15.2.jar
curl -L -o libs/jackson-datatype-jsr310-2.15.2.jar https://repo1.maven.org/maven2/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.15.2/jackson-datatype-jsr310-2.15.2.jar
curl -L -o libs/junit-platform-console-standalone-1.9.3.jar https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.9.3/junit-platform-console-standalone-1.9.3.jar
curl -L -o libs/flatlaf-3.2.jar https://repo1.maven.org/maven2/com/formdev/flatlaf/3.2/flatlaf-3.2.jar
curl -L -o libs/flatlaf-extras-3.2.jar https://repo1.maven.org/maven2/com/formdev/flatlaf-extras/3.2/flatlaf-extras-3.2.jar

# Gradle Wrapper 복구
mkdir -p gradle/wrapper
curl -L -o gradle/wrapper/gradle-wrapper.jar https://raw.githubusercontent.com/gradle/gradle/v9.4.0/gradle/wrapper/gradle-wrapper.jar

# gradlew 생성 (간소화 버전)
cat <<EOF > gradlew
#!/bin/sh
APP_BASE_NAME=\$(basename "\$0")
APP_HOME=\$(dirname "\$0")
if [ -z "\$JAVA_HOME" ]; then
    JAVA_EXE=java
else
    JAVA_EXE="\$JAVA_HOME/bin/java"
fi
CLASSPATH="\$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
exec "\$JAVA_EXE" -classpath "\$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "\$@"
EOF
chmod +x gradlew
