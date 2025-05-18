#!/bin/bash

set -e

# Paths
SRC_DIR="src"
BUILD_DIR="build"
MAIN_CLASS="com.juxtacloud.serverjars.autoupdater.Main"
JAR_NAME="server.jar"
MANIFEST_FILE="manifest.txt"

# Clean previous build
echo "[*] Cleaning build directory..."
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"

# Compile
echo "[*] Compiling Java sources..."
javac -d "$BUILD_DIR" $(find "$SRC_DIR" -name "*.java")

# Ensure manifest ends with newline
if [ -n "$(tail -c1 "$MANIFEST_FILE")" ]; then
    echo >> "$MANIFEST_FILE"
fi

# Build JAR
echo "[*] Creating JAR file..."
jar cfm "$JAR_NAME" "$MANIFEST_FILE" -C "$BUILD_DIR" .

echo "[✓] Build complete: $JAR_NAME"
