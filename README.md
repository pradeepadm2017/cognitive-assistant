# Cognitive Assistant App

## Testing on Physical Android Device

### Prerequisites
1. Enable Developer Options (tap Build Number 7 times)
2. Enable USB Debugging in Developer Options
3. Install Android Studio from https://developer.android.com/studio

### Running the App
1. Open project in Android Studio: `/home/subha/cognitive-assistant`
2. Connect your Android device via USB
3. Accept USB debugging permission on device
4. Click green "Run" button in Android Studio
5. App will build and install automatically

### Login Credentials
- **Patient**: username=`patient1`, password=`patient123`
- **Doctor**: username=`doctor1`, password=`doctor123`

### App Flow
1. **Login** → Select role → Enter credentials
2. **Patient**: Profile screen → Fill details → Test selection
3. **Doctor**: Direct to test selection
4. **Tests**: MMSE, MoCA, Rorschach (ready for implementation)

### Features
- No database (7-day in-memory storage)
- Auto-generated Patient IDs
- Modern Material 3 UI
- Navigation between screens