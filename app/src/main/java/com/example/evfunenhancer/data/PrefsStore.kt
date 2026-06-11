package com.example.evfunenhancer.data

import android.content.Context

class PrefsStore(context: Context) {
    private val prefs = context.getSharedPreferences("ev_prefs", Context.MODE_PRIVATE)

    fun getUsername(): String? = prefs.getString("username", null)
    fun setUsername(value: String?) = prefs.edit().putString("username", value).apply()

    fun getShowId(): String? = prefs.getString("show_id", null)
    fun setShowId(value: String?) = prefs.edit().putString("show_id", value).apply()

    fun getRoomCode(): String? = prefs.getString("room_code", null)
    fun setRoomCode(value: String?) = prefs.edit().putString("room_code", value).apply()

    fun getLastJoinedRoomCode(): String? = prefs.getString("last_joined_room_code", null)
    fun setLastJoinedRoomCode(value: String) = prefs.edit().putString("last_joined_room_code", value).apply()

    fun getLanguage(): String {
        if (prefs.contains("language")) return prefs.getString("language", "fi") ?: "fi"
        return if (java.util.Locale.getDefault().language == "fi") "fi" else "en"
    }
    fun setLanguage(value: String) = prefs.edit().putString("language", value).apply()

    fun hasAcceptedDisclaimer(): Boolean = prefs.getBoolean("disclaimer_accepted", false)
    fun setDisclaimerAccepted() = prefs.edit().putBoolean("disclaimer_accepted", true).apply()
}
