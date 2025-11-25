# ExitPro

**ExitPro** is a campus security management Android application that helps security guards track student entries and exits using QR code scanning. The app provides real-time monitoring of student movements, late student tracking, and secure biometric authentication for guards.

---

## 📱 What Does ExitPro Do?

ExitPro streamlines campus security operations by:

- **Scanning QR Codes**: Guards can quickly scan student ID cards to record when students leave or return to campus
- **Tracking Destinations**: Records where students are going when they exit campus
- **Monitoring Late Returns**: Identifies and lists students who return after curfew hours
- **Secure Access**: Uses OTP verification and biometric authentication to ensure only authorized guards can use the app
- **Real-time Updates**: Instantly syncs all entry/exit data with a backend server

---

## 🛠️ Technologies & Libraries

### Core Framework
- **Kotlin** (v2.0.21) - Modern programming language for Android development
- **Android SDK** - Target SDK 35 (Android 15), Minimum SDK 21 (Android 5.0)
- **Gradle** (v8.7.3) - Build automation tool

### Architecture & Design
- **MVVM Pattern** - Model-View-ViewModel architecture for clean separation of concerns
- **Kotlin Coroutines** (v1.8.0) - Handles asynchronous operations and background tasks
- **LiveData & StateFlow** - Reactive UI updates based on data changes
- **ViewBinding** - Type-safe view access without findViewById

### Networking
- **Retrofit** (v2.11.0) - REST API client for backend communication
- **OkHttp** (v4.12.0) - HTTP client with logging interceptor for network debugging
- **Gson** (v2.11.0) - JSON serialization/deserialization

### UI Components
- **Material Design 3** (v1.12.0) - Modern Android UI components and theming
- **AndroidX Libraries** - Core, AppCompat, ConstraintLayout, Fragment, Activity

### Features
- **ZXing Android Embedded** (v4.3.0) - QR code and barcode scanning functionality
- **Biometric API** (v1.4.0-alpha02) - Fingerprint and facial recognition authentication

### Lifecycle Management
- **Lifecycle Components** (v2.8.7) - ViewModel, LiveData, and lifecycle-aware components

---

## 📋 Requirements

### Development Environment
- **Android Studio** - Arctic Fox or newer (recommended: latest stable version)
- **JDK** 17 or higher
- **Gradle** 8.7.3 (included via wrapper)

### Device Requirements
- **Minimum Android Version**: Android 5.0 (API 21)
- **Target Android Version**: Android 15 (API 35)
- **Hardware**: Camera for QR scanning, Biometric sensor (optional but recommended)

### Permissions
The app requires the following permissions:
- Camera (for QR code scanning)
- Internet (for API communication)
- Phone Call (for contacting students)
- Biometric/Fingerprint (for guard authentication)
- Notifications (for alerts on Android 13+)

---

## 🚀 Installation

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/ExitPro.git
cd ExitPro
```

### 2. Open in Android Studio
1. Launch Android Studio
2. Select **File → Open**
3. Navigate to the cloned `ExitPro` directory
4. Click **OK** and wait for Gradle sync to complete

### 3. Build and Run
1. Connect an Android device or start an emulator
2. Click the **Run** button (green play icon) in Android Studio
3. Select your device and wait for installation

---

## 📖 Getting Started

### First Time Setup

1. **Launch the App**
   - The app will open to a splash screen with biometric authentication

2. **Guard Login**
   - Enter your Guard ID (provided by administration)
   - Receive an OTP on your registered device
   - Enter the OTP to complete login

3. **Home Screen**
   - After successful login, you'll see four main buttons:
     - **Scan Out**: Record student exiting campus
     - **Scan In**: Record student returning to campus
     - **Out-of-campus Students**: View list of students who are out of campus
     - **Logout**: Sign out of the app

### Basic Usage

#### Scanning Students Out
1. Tap **"Scan Out"** button
2. Point camera at student's QR code
3. Enter the student's destination when prompted
4. Confirm - data is automatically synced

#### Scanning Students In
1. Tap **"Scan In"** button
2. Point camera at student's QR code
3. Confirm - entry is recorded automatically

#### Viewing Late Students
1. Tap **"Late Students"** button
2. View list of students who returned after curfew
3. Tap phone icon to call a student if needed

## 🔌 API Integration

ExitPro communicates with a backend REST API for all data operations.

### Main Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/security/login` | PUT | Guard login with ID |
| `/security/otpMatch` | POST | OTP verification |
| `/student/gate/entry/{rollNumber}` | PUT | Record student entry |
| `/student/gate/exit` | POST | Record student exit with destination |
| `/student/out/late` | GET | Fetch list of late students |

### Authentication
- OTP-based authentication for guards
- Session managed via SharedPreferences
- Biometric re-authentication on app restart

---

## 👨‍💻 Development Notes

### Architecture Pattern
The app follows **MVVM (Model-View-ViewModel)** architecture:
- **Model**: Data classes and repository for API calls
- **View**: Activities and Fragments for UI
- **ViewModel**: Business logic and state management

### State Management
- Uses **StateFlow** for reactive UI updates
- **Sealed classes** for UI states (Idle, Loading, Success, Error)
- **Kotlin Coroutines** for async operations

### Code Style
- Kotlin coding conventions
- ViewBinding for type-safe view access
- Proper separation of concerns
- Comprehensive inline documentation

---

## 🔐 Security Features

1. **OTP Verification**: Two-factor authentication for guard login
2. **Biometric Authentication**: Fingerprint/face unlock on app restart
3. **Session Management**: Secure token storage in SharedPreferences
4. **HTTPS**: All API calls over secure connection

---

## 📦 Building Release APK

1. **Configure Signing** (if not already done):
   - Create a keystore file
   - Update `app/build.gradle` with signing config

2. **Build Release**:
```bash
./gradlew assembleRelease
```

3. **Output Location**:
   - APK: `app/build/outputs/apk/release/app-release.apk`
   - AAB: `app/build/outputs/bundle/release/app-release.aab`

---

## 🤝 Contributors

- **Sayak Mondal** - Initial development and maintenance

---

## 📄 License

This project is currently unlicensed. All rights reserved to the contributors.

---

## 📞 Support

For issues, questions, or contributions:
1. Check existing issues in the repository
2. Create a new issue with detailed description
3. Follow the contribution guidelines (if applicable)

---

## 🔄 Version History

- **v1.3** (Current) - Android 15 compatibility, improved UI/UX
- **v1.2** - Added late student tracking
- **v1.1** - Biometric authentication integration
- **v1.0** - Initial release with basic scanning functionality

---

## 🎯 Future Enhancements

- [ ] Push notifications for critical events
- [ ] Offline mode with data sync
- [ ] Analytics dashboard for security trends
- [ ] Multi-language support
- [ ] Student self-checkout feature

---

**Made with ❤️ for campus security**