package com.focuslock

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * Main Activity for Focus Lock app.
 * Handles setup, configuration, and displays goals.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var setupButton: Button
    private lateinit var goalsText: EditText
    private lateinit var saveGoalsButton: Button
    private lateinit var blockedAppsButton: Button
    private lateinit var deviceAdminButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        updateStatus()
        setupClickListeners()
        loadGoals()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun initViews() {
        statusText = findViewById(R.id.statusText)
        setupButton = findViewById(R.id.setupButton)
        goalsText = findViewById(R.id.goalsText)
        saveGoalsButton = findViewById(R.id.saveGoalsButton)
        blockedAppsButton = findViewById(R.id.blockedAppsButton)
        deviceAdminButton = findViewById(R.id.deviceAdminButton)
    }

    private fun setupClickListeners() {
        setupButton.setOnClickListener {
            openAccessibilitySettings()
        }

        saveGoalsButton.setOnClickListener {
            saveGoals()
        }

        blockedAppsButton.setOnClickListener {
            showBlockedAppsDialog()
        }

        deviceAdminButton.setOnClickListener {
            requestDeviceAdmin()
        }
    }

    private fun updateStatus() {
        val isEnabled = isAccessibilityServiceEnabled()
        val isDeviceAdmin = isDeviceAdminEnabled()

        if (isEnabled && isDeviceAdmin) {
            statusText.text = "✓ Focus Lock is ACTIVE\n\nYour distracting apps will be kicked out after 20 seconds."
            statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            setupButton.text = "Accessibility Settings"
        } else if (isEnabled && !isDeviceAdmin) {
            statusText.text = "⚠ Almost there!\n\nAccessibility enabled, but Device Admin is recommended for maximum security."
            statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
            setupButton.text = "Accessibility Settings"
        } else {
            statusText.text = "✗ Focus Lock is NOT active\n\nTap 'Enable Service' to set up."
            statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            setupButton.text = "Enable Service"
        }

        // Update device admin button
        if (isDeviceAdmin) {
            deviceAdminButton.text = "Device Admin Enabled ✓"
            deviceAdminButton.isEnabled = false
        } else {
            deviceAdminButton.text = "Enable Device Admin (Nuclear Option)"
            deviceAdminButton.isEnabled = true
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val serviceName = "${packageName}/${FocusAccessibilityService::class.java.name}"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains(serviceName) == true
    }

    private fun isDeviceAdminEnabled(): Boolean {
        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, FocusDeviceAdminReceiver::class.java)
        return devicePolicyManager.isAdminActive(componentName)
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)

        Toast.makeText(
            this,
            "Find 'Focus Lock' in the list and enable it",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun loadGoals() {
        val prefs = getSharedPreferences("focus_lock_prefs", Context.MODE_PRIVATE)
        val savedGoals = prefs.getString("user_goals", "")
        goalsText.setText(savedGoals)

        // Set hint if empty
        if (savedGoals.isNullOrEmpty()) {
            goalsText.hint = "Enter your goals and mission here...\n\nExample:\n• Get straight A's this semester\n• Build my startup\n• Improve my coding skills\n• Spend quality time with family"
        }
    }

    private fun saveGoals() {
        val goals = goalsText.text.toString()
        val prefs = getSharedPreferences("focus_lock_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("user_goals", goals).apply()

        Toast.makeText(this, "Goals saved!", Toast.LENGTH_SHORT).show()
    }

    private fun showBlockedAppsDialog() {
        val blockedApps = BlockedAppsConfig.getBlockedApps(this).toList()
        val appNames = blockedApps.map { BlockedAppsConfig.getAppDisplayName(it) }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Blocked Apps")
            .setMessage("These apps will be kicked out after 20 seconds:")
            .setItems(appNames, null)
            .setPositiveButton("OK", null)
            .setNeutralButton("Reset to Defaults") { _, _ ->
                BlockedAppsConfig.resetToDefaults(this)
                Toast.makeText(this, "Reset to default blocked apps", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun requestDeviceAdmin() {
        AlertDialog.Builder(this)
            .setTitle("Enable Device Admin?")
            .setMessage(
                "⚠️ NUCLEAR OPTION ⚠️\n\n" +
                "This will make Focus Lock VERY HARD to disable or uninstall.\n\n" +
                "You'll need to:\n" +
                "1. Go to Settings → Security → Device Admin\n" +
                "2. Manually remove Focus Lock as admin\n" +
                "3. Then you can uninstall\n\n" +
                "This is intentionally restrictive to keep you focused!\n\n" +
                "Are you sure you want this level of commitment?"
            )
            .setPositiveButton("Yes, Lock Me In") { _, _ ->
                val componentName = ComponentName(this, FocusDeviceAdminReceiver::class.java)
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                intent.putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Focus Lock needs Device Admin to prevent easy uninstallation during weak moments."
                )
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

/**
 * Device Admin Receiver for nuclear enforcement
 */
class FocusDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "Focus Lock: Device Admin Enabled - Nuclear mode active!", Toast.LENGTH_LONG).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "Focus Lock: Device Admin Disabled - You're on your own now.", Toast.LENGTH_LONG).show()
    }
}
