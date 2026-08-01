package com.example.navyalert.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import android.os.IBinder
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.navyalert.MainActivity
import com.example.navyalert.R
import com.example.navyalert.util.NotificationSettingsManager
import com.example.navyalert.util.NotificationSoundManager

class AlertSoundService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    companion object {
        const val STOP_ACTION = "com.example.navyalert.STOP_ALERT_SOUND"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "navy_alert_service_channel"
        private const val TAG = "AlertSoundService"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == STOP_ACTION) {
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        // Always call startForeground first to satisfy Android 12+ requirements
        startForegroundServiceInternal()
        
        // Handle vibration independently and immediately after foreground start
        if (NotificationSettingsManager.isVibrationEnabled(this)) {
            startVibration()
        }

        // Handle sound
        if (NotificationSettingsManager.isSoundEnabled(this)) {
            if (mediaPlayer == null || !mediaPlayer!!.isPlaying) {
                playSound()
            }
        }

        return START_STICKY
    }

    private fun startForegroundServiceInternal() {
        val stopIntent = Intent(this, AlertSoundService::class.java).apply {
            action = STOP_ACTION
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Navy Alert Active")
            .setContentText("Playing high priority escrow alert sound")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Alert", stopPendingIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun playSound() {
        try {
            // Clean up existing player if any
            mediaPlayer?.release()
            mediaPlayer = null

            val customUri = NotificationSoundManager.getSoundUri(this)
            
            if (customUri != null) {
                try {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(this@AlertSoundService, customUri)
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        isLooping = true
                        prepare()
                        start()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Custom sound playback failed", e)
                }
            }

            // Fallback to default if custom fails or wasn't set
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, R.raw.navy_alert)?.apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                        )
                    isLooping = true
                    start()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sound playback error", e)
        }
    }

    private fun startVibration() {
        try {
            Log.d(TAG, "VIBRATION_START")
            
            val localVibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            
            this.vibrator = localVibrator
            
            Log.d(TAG, "HAS_VIBRATOR=${localVibrator.hasVibrator()}")
            Log.d(TAG, "HAS_AMPLITUDE=${localVibrator.hasAmplitudeControl()}")
            
            if (localVibrator.hasVibrator()) {
                Log.d(TAG, "VIBRATOR_AVAILABLE")
                val pattern = longArrayOf(0, 500, 300, 500)
                val effect = VibrationEffect.createWaveform(pattern, 0)
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Android 13+ (API 33+)
                    localVibrator.vibrate(
                        effect,
                        VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM)
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Android 8.0 to 12
                    localVibrator.vibrate(effect)
                } else {
                    // Below Android 8: Legacy method
                    @Suppress("DEPRECATION")
                    localVibrator.vibrate(pattern, 0)
                }
                Log.d(TAG, "VIBRATION_TRIGGERED")
            } else {
                Log.d(TAG, "VIBRATOR_NOT_FOUND")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Vibration error", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Navy Alert Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Used for high priority foreground alerts"
                setSound(null, null)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
        vibrator?.cancel()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
