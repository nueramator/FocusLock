package com.focuslock

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages the list of blocked apps and whitelisted apps.
 * Uses SharedPreferences for persistent storage.
 */
object BlockedAppsConfig {

    private const val PREFS_NAME = "focus_lock_prefs"
    private const val KEY_BLOCKED_APPS = "blocked_apps"
    private const val KEY_KICK_DELAY = "kick_delay"

    // Default blocked apps (package names)
    private val DEFAULT_BLOCKED_APPS = setOf(
        "com.instagram.android",           // Instagram
        "com.google.android.youtube",      // YouTube
        "com.facebook.katana",             // Facebook
        "com.twitter.android",             // Twitter (X)
        "com.snapchat.android",            // Snapchat
        "com.zhiliaoapp.musically",        // TikTok
        "com.reddit.frontpage",            // Reddit
        "com.netflix.mediaclient",         // Netflix
        "com.spotify.music"                // Spotify
    )

    // Apps that should NEVER be blocked (essential functions)
    private val WHITELISTED_APPS = setOf(
        "com.android.dialer",              // Phone
        "com.google.android.dialer",       // Google Phone
        "com.android.contacts",            // Contacts
        "com.google.android.contacts",     // Google Contacts
        "com.android.mms",                 // Messaging
        "com.google.android.apps.messaging", // Google Messages
        "com.android.emergency",           // Emergency SOS
        "com.android.settings",            // Settings (so user can disable if absolutely needed)
        "com.focuslock"                    // Our own app
    )

    /**
     * Check if an app should be blocked
     */
    fun isBlocked(context: Context, packageName: String): Boolean {
        // Never block whitelisted apps
        if (WHITELISTED_APPS.contains(packageName)) {
            return false
        }

        // Check if in blocked list
        val blockedApps = getBlockedApps(context)
        return blockedApps.contains(packageName)
    }

    /**
     * Get the current list of blocked apps
     */
    fun getBlockedApps(context: Context): Set<String> {
        val prefs = getPrefs(context)
        val savedApps = prefs.getStringSet(KEY_BLOCKED_APPS, null)

        // If no saved list, return defaults
        return savedApps ?: DEFAULT_BLOCKED_APPS
    }

    /**
     * Set the list of blocked apps
     */
    fun setBlockedApps(context: Context, apps: Set<String>) {
        getPrefs(context)
            .edit()
            .putStringSet(KEY_BLOCKED_APPS, apps)
            .apply()
    }

    /**
     * Add an app to the blocked list
     */
    fun addBlockedApp(context: Context, packageName: String) {
        val currentApps = getBlockedApps(context).toMutableSet()
        currentApps.add(packageName)
        setBlockedApps(context, currentApps)
    }

    /**
     * Remove an app from the blocked list
     */
    fun removeBlockedApp(context: Context, packageName: String) {
        val currentApps = getBlockedApps(context).toMutableSet()
        currentApps.remove(packageName)
        setBlockedApps(context, currentApps)
    }

    /**
     * Reset to default blocked apps
     */
    fun resetToDefaults(context: Context) {
        setBlockedApps(context, DEFAULT_BLOCKED_APPS)
    }

    /**
     * Get the kick-out delay in milliseconds
     */
    fun getKickDelay(context: Context): Long {
        return getPrefs(context).getLong(KEY_KICK_DELAY, 20000L) // Default 20 seconds
    }

    /**
     * Set the kick-out delay in milliseconds
     */
    fun setKickDelay(context: Context, delayMs: Long) {
        getPrefs(context)
            .edit()
            .putLong(KEY_KICK_DELAY, delayMs)
            .apply()
    }

    /**
     * Get SharedPreferences instance
     */
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Get user-friendly name for a package
     */
    fun getAppDisplayName(packageName: String): String {
        return when (packageName) {
            "com.instagram.android" -> "Instagram"
            "com.google.android.youtube" -> "YouTube"
            "com.facebook.katana" -> "Facebook"
            "com.twitter.android" -> "Twitter (X)"
            "com.snapchat.android" -> "Snapchat"
            "com.zhiliaoapp.musically" -> "TikTok"
            "com.reddit.frontpage" -> "Reddit"
            "com.netflix.mediaclient" -> "Netflix"
            "com.spotify.music" -> "Spotify"
            else -> packageName
        }
    }

    /**
     * Get list of default blocked apps for UI display
     */
    fun getDefaultBlockedApps(): Set<String> {
        return DEFAULT_BLOCKED_APPS
    }
}
