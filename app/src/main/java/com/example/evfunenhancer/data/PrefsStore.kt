package com.example.evfunenhancer.data

import android.content.Context

class PrefsStore(context: Context) {
    private val prefs = context.getSharedPreferences("ev_prefs", Context.MODE_PRIVATE)

    // Test database mode (Maintenance screen). Read once: switching restarts the app.
    val testMode: Boolean = prefs.getBoolean("test_mode", false)
    // commit() rather than apply(): the process is killed right after for the restart.
    fun setTestMode(value: Boolean) = prefs.edit().putBoolean("test_mode", value).commit()

    // Room and show are per environment, so switching modes keeps your place in each.
    private fun envKey(key: String) = if (testMode) "test_$key" else key

    fun getUsername(): String? = prefs.getString("username", null)
    fun setUsername(value: String?) = prefs.edit().putString("username", value).apply()

    fun getShowId(): String? = prefs.getString(envKey("show_id"), null)
    fun setShowId(value: String?) = prefs.edit().putString(envKey("show_id"), value).apply()

    fun getRoomCode(): String? = prefs.getString(envKey("room_code"), null)
    fun setRoomCode(value: String?) = prefs.edit().putString(envKey("room_code"), value).apply()

    fun getLastJoinedRoomCode(): String? = prefs.getString(envKey("last_joined_room_code"), null)
    fun setLastJoinedRoomCode(value: String) = prefs.edit().putString(envKey("last_joined_room_code"), value).apply()

    fun getLanguage(): String {
        if (prefs.contains("language")) return prefs.getString("language", "fi") ?: "fi"
        return if (java.util.Locale.getDefault().language == "fi") "fi" else "en"
    }
    fun setLanguage(value: String) = prefs.edit().putString("language", value).apply()

    fun hasAcceptedDisclaimer(): Boolean = prefs.getBoolean("disclaimer_accepted", false)
    fun setDisclaimerAccepted() = prefs.edit().putBoolean("disclaimer_accepted", true).apply()

    fun hasSeenAftershowStory(year: Int): Boolean = prefs.getBoolean("aftershow_story_seen_$year", false)
    fun setAftershowStorySeen(year: Int, seen: Boolean = true) = prefs.edit().putBoolean("aftershow_story_seen_$year", seen).apply()
}
