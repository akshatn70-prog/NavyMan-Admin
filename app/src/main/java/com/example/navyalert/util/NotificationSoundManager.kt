package com.example.navyalert.util

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri

object NotificationSoundManager {
    private const val PREF_NAME = "notification_sound_prefs"
    private const val KEY_SOUND_URI = "escrow_sound_uri"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveSoundUri(context: Context, uri: Uri) {
        getPrefs(context).edit().putString(KEY_SOUND_URI, uri.toString()).apply()
    }

    fun getSoundUri(context: Context): Uri? {
        val uriString = getPrefs(context).getString(KEY_SOUND_URI, null)
        return if (uriString != null) Uri.parse(uriString) else null
    }

    fun clearSound(context: Context) {
        getPrefs(context).edit().remove(KEY_SOUND_URI).apply()
    }
}
