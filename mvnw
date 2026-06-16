#!/bin/sh
# Maven Wrapper startup script for UN*X

# Determine the project base directory
MAVEN_PROJECTBASEDIR=$(cd "$(dirname "$0")" && pwd)
export MAVEN_PROJECTBASEDIR

# Maven wrapper JAR location
WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

# Download Maven wrapper if not present
if [ ! -e "$WRAPPER_JAR" ]; then
    echo "Downloading Maven Wrapper..."
    if command -v wget > /dev/null; then
        wget -q -O "$WRAPPER_JAR" "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
    elif command -v curl > /dev/null; then
        curl -s -o "$WRAPPER_JAR" "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
    else
        echo "Error: Neither wget nor curl found. Please install Maven manually."
        exit 1
    fi
fi

# Run Maven with wrapper
java -jar "$WRAPPER_JAR" "$@"