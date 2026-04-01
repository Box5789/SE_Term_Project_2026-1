#!/bin/sh
APP_BASE_NAME=$(basename "$0")
APP_HOME=$(dirname "$0")
if [ -z "$JAVA_HOME" ]; then
    JAVA_EXE=java
else
    JAVA_EXE="$JAVA_HOME/bin/java"
fi
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
exec "$JAVA_EXE" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
