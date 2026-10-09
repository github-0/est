package com.example.evfunenhancer.data

// The real show IDs, in broadcast order. admin.py hides shows by renaming them (e.g.
// shows/final_test), so any other document ID in shows/ is ignored.
val SHOW_IDS = listOf("semi1", "semi2", "final")

data class Participant(
    val order: Int = 0,
    val country: String = "",
    val artist: String = "",
    val song: String = ""
)
