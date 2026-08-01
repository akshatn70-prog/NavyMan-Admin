package com.example.navyalert.util

import android.content.Context
import android.content.SharedPreferences
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.navyalert.service.QuietHoursWorker
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object QuietHoursManager {
    private const val PREF_NAME = "quiet_hours_prefs"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_START_TIME = "start_time"
    private const val KEY_END_TIME = "end_time"
    private const val KEY_MISSED_NOTIFICATIONS = "missed_notifications"
    private const val WORK_NAME = "QuietHoursSummaryWork"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
        if (!enabled) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    fun isEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ENABLED, false)
    }

    fun setStartTime(context: Context, time: String) {
        getPrefs(context).edit().putString(KEY_START_TIME, time).apply()
    }

    fun getStartTime(context: Context): String {
        return getPrefs(context).getString(KEY_START_TIME, "23:00") ?: "23:00"
    }

    fun setEndTime(context: Context, time: String) {
        getPrefs(context).edit().putString(KEY_END_TIME, time).apply()
    }

    fun getEndTime(context: Context): String {
        return getPrefs(context).getString(KEY_END_TIME, "08:00") ?: "08:00"
    }

    fun saveMissedNotification(context: Context, title: String, body: String) {
        val missedSet = getPrefs(context).getStringSet(KEY_MISSED_NOTIFICATIONS, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        val timestamp = System.currentTimeMillis()
        val entry = "$timestamp|$title|$body"
        missedSet.add(entry)
        getPrefs(context).edit().putStringSet(KEY_MISSED_NOTIFICATIONS, missedSet).apply()
        
        scheduleSummaryNotification(context)
    }

    fun getMissedCount(context: Context): Int {
        return getPrefs(context).getStringSet(KEY_MISSED_NOTIFICATIONS, null)?.size ?: 0
    }

    fun resetMissedCount(context: Context) {
        getPrefs(context).edit().remove(KEY_MISSED_NOTIFICATIONS).apply()
    }

    fun isCurrentlyInQuietHours(context: Context): Boolean {
        if (!isEnabled(context)) return false

        val now = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val start = LocalTime.parse(getStartTime(context), formatter)
        val end = LocalTime.parse(getEndTime(context), formatter)

        return if (start.isBefore(end)) {
            // Normal range (e.g., 09:00 to 17:00)
            now.isAfter(start) && now.isBefore(end)
        } else {
            // Overnight range (e.g., 23:00 to 08:00)
            now.isAfter(start) || now.isBefore(end)
        }
    }

    private fun scheduleSummaryNotification(context: Context) {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val end = LocalTime.parse(getEndTime(context), formatter)
        val now = LocalTime.now()

        val delay = if (now.isBefore(end)) {
            Duration.between(now, end)
        } else {
            Duration.between(now, end.plusHours(24))
        }

        val workRequest = OneTimeWorkRequestBuilder<QuietHoursWorker>()
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
