package com.focuslock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast

/**
 * Boot Receiver that checks if Focus Lock is enabled after device reboot.
 * Accessibility services persist across reboots, but this helps verify status.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "FocusLockBoot"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {

            Log.d(TAG, "Device booted, checking Focus Lock status")

            // Check if accessibility service is still enabled
            val isEnabled = isAccessibilityServiceEnabled(context)

            if (isEnabled) {
                Log.d(TAG, "Focus Lock is active after reboot")
                // Service will automatically restart
            } else {
                Log.w(TAG, "Focus Lock is NOT active after reboot")
                // Note: We can't automatically re-enable accessibility service
                // User must manually enable it in settings
            }
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val serviceName = "${context.packageName}/${FocusAccessibilityService::class.java.name}"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains(serviceName) == true
    }
}
