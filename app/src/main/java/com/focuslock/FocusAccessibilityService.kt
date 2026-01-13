package com.focuslock

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

/**
 * Core Accessibility Service that monitors foreground apps and
 * automatically kicks out distracting apps after 20 seconds.
 */
class FocusAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var kickOutRunnable: Runnable? = null
    private var currentBlockedApp: String? = null
    private var kickOutDelay = 20000L // 20 seconds in milliseconds

    companion object {
        private const val TAG = "FocusAccessibility"
        private const val NOTIFICATION_CHANNEL_ID = "focus_lock_service"
        private const val NOTIFICATION_ID = 1
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "FocusAccessibilityService connected")

        // Create notification channel for Android O and above
        createNotificationChannel()

        // Start as foreground service
        startForeground(NOTIFICATION_ID, createNotification("Focus Lock Active"))
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Only process window state changes (when apps come to foreground)
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return

        // Ignore our own app
        if (packageName == "com.focuslock") {
            cancelKickOut()
            return
        }

        Log.d(TAG, "App in foreground: $packageName")

        // Check if this app should be blocked
        if (BlockedAppsConfig.isBlocked(this, packageName)) {
            handleBlockedApp(packageName)
        } else {
            // Not a blocked app, cancel any pending kick-out
            cancelKickOut()
        }
    }

    /**
     * Handle when a blocked app comes to foreground
     */
    private fun handleBlockedApp(packageName: String) {
        // If already tracking this app, don't restart timer
        if (currentBlockedApp == packageName && kickOutRunnable != null) {
            Log.d(TAG, "Already tracking $packageName")
            return
        }

        // Cancel any existing timer
        cancelKickOut()

        currentBlockedApp = packageName

        Log.d(TAG, "Starting timer for blocked app: $packageName")

        // Create new kick-out task
        kickOutRunnable = Runnable {
            kickOutApp(packageName)
        }

        // Schedule kick-out after delay
        handler.postDelayed(kickOutRunnable!!, kickOutDelay)

        // Update notification to show which app is being monitored
        val appName = getAppName(packageName)
        updateNotification("Monitoring: $appName (${kickOutDelay / 1000}s)")
    }

    /**
     * Kick out the current app by going to home screen
     */
    private fun kickOutApp(packageName: String) {
        Log.d(TAG, "Kicking out app: $packageName")

        // Perform global action to go home
        val success = performGlobalAction(GLOBAL_ACTION_HOME)

        if (success) {
            Log.d(TAG, "Successfully kicked out $packageName")
            updateNotification("Kicked out: ${getAppName(packageName)}")
        } else {
            Log.e(TAG, "Failed to kick out $packageName")
        }

        // Clear current tracked app
        currentBlockedApp = null
        kickOutRunnable = null

        // Reset notification after a delay
        handler.postDelayed({
            updateNotification("Focus Lock Active")
        }, 3000)
    }

    /**
     * Cancel the pending kick-out
     */
    private fun cancelKickOut() {
        kickOutRunnable?.let {
            handler.removeCallbacks(it)
            kickOutRunnable = null
            currentBlockedApp = null
            Log.d(TAG, "Cancelled kick-out timer")
        }
    }

    /**
     * Get user-friendly app name from package name
     */
    private fun getAppName(packageName: String): String {
        return try {
            val packageManager = packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Focus Lock Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification for Focus Lock service status"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(message: String): android.app.Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Focus Lock")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock) // Using system icon for now
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(message: String) {
        val notification = createNotification(message)
        val notificationManager = NotificationManagerCompat.from(this)

        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to update notification", e)
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "FocusAccessibilityService interrupted")
        cancelKickOut()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "FocusAccessibilityService destroyed")
        cancelKickOut()
    }
}
