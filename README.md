# Focus Lock 🔒

**Stop wasting time. Stay locked in on your mission.**

Focus Lock is an Android app that makes distracting apps (Instagram, YouTube, etc.) essentially unusable by automatically kicking you out after 20 seconds. Essential communication apps like Phone and Messages remain fully functional.

## Why Focus Lock?

- **Nuclear Enforcement**: No bypasses, no "just 5 more minutes"
- **Smart Monitoring**: Uses Accessibility Service to detect which apps you're using
- **Keep What Matters**: Calling and texting work normally
- **Remember Your Why**: Built-in goals tracker to keep you motivated
- **Survives Reboots**: Service automatically restarts after device reboot
- **Optional Device Admin**: Makes the app extremely hard to disable during weak moments

## How It Works

1. Focus Lock runs as an Accessibility Service in the background
2. When you open Instagram, YouTube, or other blocked apps, a 20-second timer starts
3. After 20 seconds, you're automatically sent to the home screen
4. The cycle repeats if you open the app again
5. Result: Distracting apps become too annoying to use, but you can still make calls and send texts

## Installation

### Prerequisites

- Android device running Android 8.0 (API 26) or higher
- Android Studio (latest version recommended)
- Physical Android device for testing (Accessibility Services don't work well in emulators)

### Setup Steps

1. **Clone this repository:**
   ```bash
   git clone https://github.com/yourusername/FocusLock.git
   cd FocusLock
   ```

2. **Open in Android Studio:**
   - Launch Android Studio
   - File → Open → Select the `FocusApp` folder
   - Wait for Gradle sync to complete

3. **Connect your Android phone:**
   - Enable Developer Options on your phone (Settings → About Phone → Tap Build Number 7 times)
   - Enable USB Debugging (Settings → Developer Options → USB Debugging)
   - Connect phone via USB
   - Allow debugging when prompted

4. **Build and install:**
   - Click the green "Run" button in Android Studio
   - Or use command line: `./gradlew installDebug`

5. **Enable Accessibility Service:**
   - Open the Focus Lock app
   - Tap "Enable Service"
   - Find "Focus Lock" in the Accessibility settings
   - Enable it
   - Grant all requested permissions

6. **Set your goals:**
   - Return to the Focus Lock app
   - Enter your mission and goals
   - Tap "Save Goals"

7. **(Optional) Enable Device Admin:**
   - Tap "Enable Device Admin (Nuclear Option)"
   - Read the warning carefully
   - If you're committed, tap "Yes, Lock Me In"
   - This makes Focus Lock VERY difficult to disable or uninstall

## Features

### Core Features
- ✅ Automatic kick-out after 20 seconds for blocked apps
- ✅ Configurable blocked apps list
- ✅ Whitelisted essential apps (Phone, Messages, Contacts)
- ✅ Goals/mission tracker to keep you motivated
- ✅ Persistent background service
- ✅ Boot receiver (survives reboots)
- ✅ Material Design 3 UI with dark mode

### Blocked Apps by Default
- Instagram
- YouTube
- Facebook
- Twitter (X)
- Snapchat
- TikTok
- Reddit
- Netflix
- Spotify

### Always Available Apps
- Phone (all dialer apps)
- Messages (SMS/texting)
- Contacts
- Emergency services
- Settings (so you can disable if absolutely necessary)

## Usage

### Normal Usage
1. Just use your phone normally
2. Essential apps work without restriction
3. Distracting apps automatically close after 20 seconds
4. Open Focus Lock anytime to review/edit your goals

### To Temporarily Disable (Emergency)
1. Go to Settings → Accessibility
2. Find Focus Lock
3. Disable it
4. **Note:** With Device Admin enabled, you'll need to remove admin privileges first

### To Completely Uninstall
1. Open Settings → Security → Device Admin
2. Deactivate Focus Lock
3. Go to Settings → Apps
4. Uninstall Focus Lock

## Architecture

### Key Components

**FocusAccessibilityService.kt**
- Core service that monitors foreground apps
- Implements the 20-second timer
- Performs the "go home" action to kick you out

**BlockedAppsConfig.kt**
- Manages which apps are blocked/whitelisted
- Stores configuration in SharedPreferences
- Provides app display names

**MainActivity.kt**
- Main UI for setup and configuration
- Displays service status
- Goals editor
- Device admin setup

**BootReceiver.kt**
- Restarts monitoring after device reboot
- Verifies service status on boot

## Configuration

### Change the Timer Duration
Edit `FocusAccessibilityService.kt` (line 14):
```kotlin
private var kickOutDelay = 20000L // Change to desired milliseconds
```

### Add/Remove Blocked Apps
Edit `BlockedAppsConfig.kt` to modify the `DEFAULT_BLOCKED_APPS` set with package names.

To find an app's package name:
```bash
adb shell pm list packages | grep <app-name>
```

### Customize Colors/Theme
Edit files in `app/src/main/res/values/`:
- `colors.xml` - Color definitions
- `themes.xml` - Light theme
- `values-night/themes.xml` - Dark theme

## Technical Details

### Permissions Required
- `BIND_ACCESSIBILITY_SERVICE` - Monitor and interact with apps
- `FOREGROUND_SERVICE` - Keep service running
- `RECEIVE_BOOT_COMPLETED` - Restart after reboot
- `POST_NOTIFICATIONS` - Show service status notification

### Minimum SDK
- API 26 (Android 8.0 Oreo)
- Target SDK 34 (Android 14)

### Technologies Used
- Kotlin
- AndroidX libraries
- Material Design 3
- Accessibility Service API
- Device Admin API

## Troubleshooting

### Service Not Working
1. Verify Accessibility Service is enabled in Settings
2. Check that app has all required permissions
3. Restart your phone
4. Reinstall the app

### Apps Not Being Blocked
1. Verify the app's package name is in the blocked list
2. Check that the app isn't whitelisted
3. Review logcat for debug messages: `adb logcat -s FocusAccessibility`

### Can't Uninstall
1. This is intentional if Device Admin is enabled
2. Go to Settings → Security → Device Admin
3. Deactivate Focus Lock
4. Then uninstall normally

### Accessibility Service Disabled Automatically
- Some manufacturers aggressively kill background services
- Disable battery optimization for Focus Lock
- Settings → Apps → Focus Lock → Battery → Unrestricted

## Development

### Build Variants
```bash
./gradlew assembleDebug    # Debug build
./gradlew assembleRelease  # Release build (requires signing)
```

### Run Tests
```bash
./gradlew test
```

### Generate Signed APK
1. Build → Generate Signed Bundle / APK
2. Follow Android Studio wizard
3. Keep your keystore file safe!

## Contributing

This is a personal focus tool, but contributions are welcome:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly on a physical device
5. Submit a pull request

## Privacy & Security

- **No data collection**: Focus Lock doesn't collect, store, or transmit any personal data
- **Local only**: All settings and goals stored locally on your device
- **No internet**: App doesn't require or use internet connection
- **Open source**: Full source code available for audit

## License

This project is open source and available under the MIT License.

## Disclaimer

This app is a productivity tool designed to help you stay focused. It is **intentionally difficult to disable** when Device Admin is enabled. Only enable Device Admin if you're committed to your goals and understand the implications.

Use responsibly. Focus Lock is not responsible for any consequences of using this app.

## Support

Having issues? Found a bug?

1. Check the Troubleshooting section above
2. Search existing issues
3. Open a new issue with:
   - Android version
   - Device model
   - Detailed description of the problem
   - Steps to reproduce

## Roadmap

Potential future features:
- [ ] Custom timer per app
- [ ] Time-based blocking (e.g., only after 2:30 PM)
- [ ] Usage statistics
- [ ] Widget for quick status check
- [ ] Goal progress tracking
- [ ] Motivational quotes/reminders
- [ ] Export/import configuration

## Acknowledgments

Built with determination to overcome phone addiction and stay focused on what truly matters.

---

**Stay locked in. Achieve your goals. 🎯**
