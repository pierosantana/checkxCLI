#!/bin/bash

# CheckX Installation Script
# Builds the project and sets up the alias

set -e

echo "╔════════════════════════════════════════╗"
echo "║     CheckX Installation Script         ║"
echo "╚════════════════════════════════════════╝"
echo ""

# Check Java version
echo "→ Checking Java version..."
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 17 or higher."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17 or higher required. Found version: $JAVA_VERSION"
    exit 1
fi
echo "✓ Java $JAVA_VERSION detected"

# Check Maven
echo "→ Checking Maven..."
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven not found. Please install Maven 3.6+."
    exit 1
fi
echo "✓ Maven detected"

# Build project
echo ""
echo "→ Building CheckX..."
mvn clean package -q

if [ ! -f "target/checkx.jar" ]; then
    echo "❌ Build failed. JAR not found."
    exit 1
fi
echo "✓ Build successful"

# Get project directory
PROJECT_DIR=$(pwd)

# Detect shell
SHELL_CONFIG=""
if [ -f "$HOME/.zshrc" ]; then
    SHELL_CONFIG="$HOME/.zshrc"
elif [ -f "$HOME/.bashrc" ]; then
    SHELL_CONFIG="$HOME/.bashrc"
else
    echo "⚠️  Could not detect .bashrc or .zshrc"
fi

# Add alias
echo ""
echo "→ Setting up alias..."
ALIAS_LINE="alias checkx='java -jar $PROJECT_DIR/target/checkx.jar'"

if [ -n "$SHELL_CONFIG" ]; then
    if grep -q "alias checkx=" "$SHELL_CONFIG"; then
        echo "⚠️  Alias already exists in $SHELL_CONFIG"
    else
        echo "" >> "$SHELL_CONFIG"
        echo "# CheckX alias" >> "$SHELL_CONFIG"
        echo "$ALIAS_LINE" >> "$SHELL_CONFIG"
        echo "✓ Alias added to $SHELL_CONFIG"
    fi
fi

# Summary
echo ""
echo "╔════════════════════════════════════════╗"
echo "║        Installation Complete! 🎉       ║"
echo "╚════════════════════════════════════════╝"
echo ""
echo "To start using CheckX:"
echo ""
echo "  1. Reload your shell:"
if [ -n "$SHELL_CONFIG" ]; then
    echo "     source $SHELL_CONFIG"
fi
echo ""
echo "  2. Add your first habit:"
echo "     checkx add \"Exercise\""
echo ""
echo "  3. View today's habits:"
echo "     checkx daily"
echo ""
echo "  4. Get help:"
echo "     checkx --help"
echo ""
echo "Happy habit tracking! 🔥"
