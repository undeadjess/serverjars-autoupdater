#!/bin/bash

set -e

# Paths
SRC_DIR="src"
BUILD_DIR="build"
LIB_DIR="libs"
MAIN_CLASS="com.juxtacloud.serverjars.autoupdater.Main"
JAR_NAME="server.jar"
MANIFEST_FILE="manifest.txt"

# Clean previous build
echo "[*] Cleaning build directory..."
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"

# Compile source files
echo "[*] Compiling Java sources..."
javac -cp "$LIB_DIR/json.jar" -d "$BUILD_DIR" "$SRC_DIR/com/juxtacloud/serverjars/autoupdater/"*.java

# Unpack JSON library into build dir
echo "[*] Unpacking JSON library..."
cd "$BUILD_DIR"
jar xf "../$LIB_DIR/json.jar"
cd ..

# Ensure manifest ends with newline
if [ -n "$(tail -c1 "$MANIFEST_FILE")" ]; then
    echo >> "$MANIFEST_FILE"
fi

# Build the JAR
echo "[*] Creating JAR file..."
jar cfm "$JAR_NAME" "$MANIFEST_FILE" -C "$BUILD_DIR" .

echo "[✓] Build complete: $JAR_NAME"
