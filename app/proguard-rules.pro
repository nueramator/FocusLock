# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep accessibility service
-keep class com.focuslock.FocusAccessibilityService { *; }
-keep class com.focuslock.FocusDeviceAdminReceiver { *; }

# Keep all classes that extend Service
-keep public class * extends android.app.Service
-keep public class * extends android.accessibilityservice.AccessibilityService
