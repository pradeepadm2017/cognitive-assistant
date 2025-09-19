# Testing the Cognitive Assistant App

## Quick Test Method

### Option 1: Direct APK Download
Since building requires Java 17, here are alternative testing methods:

1. **Upload to Online Builder**:
   - Go to https://appetize.io (free online Android emulator)
   - Upload the project folder as ZIP
   - Test directly in browser

2. **Use GitHub Actions** (Recommended):
   ```bash
   # Create GitHub repo
   git init
   git add .
   git commit -m "Initial commit"
   git remote add origin YOUR_GITHUB_REPO
   git push -u origin main
   ```

   Then add this to `.github/workflows/build.yml`:
   ```yaml
   name: Build APK
   on: [push]
   jobs:
     build:
       runs-on: ubuntu-latest
       steps:
         - uses: actions/checkout@v3
         - uses: actions/setup-java@v3
           with:
             java-version: '17'
             distribution: 'temurin'
         - run: chmod +x gradlew
         - run: ./gradlew assembleDebug
         - uses: actions/upload-artifact@v3
           with:
             name: app-debug
             path: app/build/outputs/apk/debug/app-debug.apk
   ```

### Option 2: Manual Installation via File Manager

1. **Copy project to phone**:
   - Zip the entire `/home/subha/cognitive-assistant` folder
   - Transfer to phone via USB/email/cloud

2. **Install APK Installer app** on phone (from Play Store)

3. **Use Termux** (Android terminal):
   - Install Termux from Play Store
   - Install Java: `pkg install openjdk-17`
   - Build APK directly on phone

### Login Credentials for Testing:
- **Patient**: `patient1` / `patient123`
- **Doctor**: `doctor1` / `doctor123`

### Expected App Flow:
1. Login screen with role selection
2. Patient → Profile form → Test selection
3. Doctor → Direct to test selection
4. Three test cards: MMSE, MoCA, Rorschach