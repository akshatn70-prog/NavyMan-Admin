package com.example.navyalert.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object FCMTokenManager {
    private const val PREF_NAME = "navy_alert_prefs"
    private const val KEY_FCM_TOKEN = "fcm_token"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(context: Context, token: String) {
        getPrefs(context).edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    fun getSavedToken(context: Context): String? {
        return getPrefs(context).getString(KEY_FCM_TOKEN, null)
    }

    fun clearToken(context: Context) {
        getPrefs(context).edit().remove(KEY_FCM_TOKEN).apply()
    }

    /**
     * Registers the FCM token with the backend server.
     * Temporarily always performs the request for debugging.
     * Retries once on failure.
     */
    fun registerTokenWithServer(context: Context, token: String) {
        Log.d("FCM_SERVER", "registerTokenWithServer() called")
        
        // Debugging: Always attempt to register with server regardless of local storage
        Log.d("FCM_SERVER", "Bypassing local check for debugging. Always sending POST.")

        CoroutineScope(Dispatchers.IO).launch {
            var success = false
            var attempt = 0
            val maxAttempts = 2

            while (attempt < maxAttempts && !success) {
                attempt++
                var connection: HttpURLConnection? = null
                try {
                    val urlString = "https://navymanescrowbot.onrender.com/register-token"
                    Log.d("FCM_SERVER", "URL: $urlString")
                    Log.d("FCM_SERVER", "Creating URL")
                    Log.d("FCM_SERVER", "Sending POST request")
                    val url = URL(urlString)
                    
                    Log.d("FCM_SERVER", "Creating HttpURLConnection")
                    connection = url.openConnection() as HttpURLConnection
                    Log.d("FCM_SERVER", "HttpURLConnection created")
                    connection.apply {
                        requestMethod = "POST"
                        setRequestProperty("Content-Type", "application/json")
                        doOutput = true
                        connectTimeout = 15000
                        readTimeout = 15000
                    }

                    Log.d("FCM_SERVER", "Calling connect()")
                    connection.connect()

                    // Write body to output stream
                    connection.outputStream.use { os ->
                        OutputStreamWriter(os, "UTF-8").use { writer ->
                            writer.write("{\"token\":\"$token\"}")
                            writer.flush()
                        }
                    }

                    val responseCode = connection.responseCode
                    Log.d("FCM_SERVER", "Response code = $responseCode")
                    
                    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                        Log.d("FCM_SERVER", "Success")
                        Log.d("FCM_SERVER", "Token registered successfully on attempt $attempt")
                        saveToken(context, token)
                        success = true
                    } else {
                        Log.e("FCM_SERVER", "Failed")
                        Log.e("FCM_SERVER", "Server returned error code $responseCode on attempt $attempt")
                    }
                } catch (e: Exception) {
                    Log.e("FCM_SERVER", "Exception", e)
                    Log.e("FCM_SERVER", "Network error on attempt $attempt: ${e.message}")
                } finally {
                    connection?.disconnect()
                }
            }

            if (!success) {
                Log.e("FCM_SERVER", "Failed to register token after $maxAttempts attempts.")
            }
        }
    }
}
