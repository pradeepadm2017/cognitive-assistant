#!/bin/bash

echo "=== Cognitive Assistant - Device Installation ==="
echo ""

# Check if adb is available
if ! command -v adb &> /dev/null; then
    echo "❌ ADB not found. Installing platform tools..."

    # Download platform tools
    wget -q https://dl.google.com/android/repository/platform-tools-latest-linux.zip -O /tmp/platform-tools.zip

    # Extract using Python (no unzip needed)
    python3 -c "
import zipfile
import os
with zipfile.ZipFile('/tmp/platform-tools.zip', 'r') as zip_ref:
    zip_ref.extractall(os.path.expanduser('~'))
"

    # Add to PATH
    export PATH=$PATH:~/platform-tools
    echo "✅ Platform tools installed"
fi

# Check device connection
echo "📱 Checking device connection..."
adb devices

# Instructions
echo ""
echo "📋 SETUP INSTRUCTIONS:"
echo "1. Enable Developer Options on your phone:"
echo "   Settings → About Phone → Tap 'Build Number' 7 times"
echo ""
echo "2. Enable USB Debugging:"
echo "   Settings → Developer Options → USB Debugging ON"
echo ""
echo "3. Connect phone via USB and accept debugging permission"
echo ""
echo "4. Run this script again to install the app"

# Check if device is connected
if adb devices | grep -q "device$"; then
    echo ""
    echo "🎉 Device detected! Ready to install app."
    echo ""
    echo "To build and install:"
    echo "cd /home/subha/cognitive-assistant"
    echo "./gradlew assembleDebug"
    echo "adb install app/build/outputs/apk/debug/app-debug.apk"
else
    echo ""
    echo "⚠️  No device detected. Please follow steps above."
fi