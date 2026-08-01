package com.example.navyalert.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.navyalert.util.NotificationHelper
import com.example.navyalert.util.QuietHoursManager

class QuietHoursWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val missedCount = QuietHoursManager.getMissedCount(applicationContext)
        if (missedCount > 0) {
            // Show summary notification
            NotificationHelper.showNotification(
                applicationContext,
                "You have $missedCount missed escrow alerts",
                "Tap to view missed notifications."
            )
            // Reset count
            QuietHoursManager.resetMissedCount(applicationContext)
        }
        return Result.success()
    }
}
