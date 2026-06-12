package com.example.evfunenhancer.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant

data class UpdateInfo(val latestVersion: String, val releaseUrl: String)

sealed class UpdateCheckResult {
    object Pending : UpdateCheckResult()
    data class UpToDate(val checkedAt: Instant) : UpdateCheckResult()
    data class Available(val info: UpdateInfo, val checkedAt: Instant) : UpdateCheckResult()
    data class Failed(val checkedAt: Instant) : UpdateCheckResult()
}

internal fun isNewerVersion(current: String, latest: String): Boolean {
    return try {
        val cur = current.trimStart('v').split(".").map { it.toInt() }
        val lat = latest.trimStart('v').split(".").map { it.toInt() }
        val size = maxOf(cur.size, lat.size)
        for (i in 0 until size) {
            val c = cur.getOrElse(i) { 0 }
            val l = lat.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        false
    } catch (e: Exception) {
        false
    }
}

suspend fun checkForUpdate(currentVersion: String): UpdateCheckResult = withContext(Dispatchers.IO) {
    val checkedAt = Instant.now()
    try {
        val url = URL("https://api.github.com/repos/github-0/est/releases/latest")
        val conn = url.openConnection() as HttpURLConnection
        conn.setRequestProperty("User-Agent", "ev-app")
        conn.setRequestProperty("Accept", "application/vnd.github+json")
        conn.connectTimeout = 5_000
        conn.readTimeout = 5_000
        if (conn.responseCode != 200) return@withContext UpdateCheckResult.Failed(checkedAt)
        val body = conn.inputStream.bufferedReader().readText()
        val json = JSONObject(body)
        val tagName = json.getString("tag_name")
        val htmlUrl = json.getString("html_url")
        if (isNewerVersion(currentVersion, tagName)) {
            UpdateCheckResult.Available(UpdateInfo(latestVersion = tagName.trimStart('v'), releaseUrl = htmlUrl), checkedAt)
        } else {
            UpdateCheckResult.UpToDate(checkedAt)
        }
    } catch (e: Exception) {
        UpdateCheckResult.Failed(checkedAt)
    }
}
