package com.zacharee1.boredsigns.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import com.zacharee1.boredsigns.R
import com.zacharee1.boredsigns.util.Recorder
import com.zacharee1.boredsigns.util.Utils
import com.zacharee1.boredsigns.widgets.RecorderWidget


class RecorderService : Service() {
    companion object {
        val BASE = "com.zacharee1.boredsigns.action.RECORDER"

        val ACTION_RECORD = BASE + "_RECORD"
        val ACTION_PLAY = BASE + "_PLAY"
        val ACTION_STOP = BASE + "_STOP"

        val FILTER = object : IntentFilter() {
            init {
                addAction(ACTION_RECORD)
                addAction(ACTION_PLAY)
                addAction(ACTION_STOP)
                addAction(NotificationManager.ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED)
            }
        }
    }

    private var recorder = Recorder()

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startForeground()

        contentResolver.registerContentObserver(Settings.System.CONTENT_URI, true, systemObserver)
        contentResolver.registerContentObserver(Settings.Global.CONTENT_URI, true, systemObserver)
        registerReceiver(receiver, FILTER)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)

        try {
            unregisterReceiver(receiver)
            recorder.stop()
        } catch (e: Exception) {}

        contentResolver.unregisterContentObserver(systemObserver)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_RECORD) {
                record()
            }
            if (intent?.action == ACTION_PLAY) {
                play()
            }

            if (intent?.action == ACTION_STOP) {
                stop()
            }

            if (intent?.action == NotificationManager.ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED) {
                updateAll()
            }
        }
    }

    private val systemObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            if (uri!!.toString().contains("volume") /*|| uri == Settings.Global.getUriFor(Settings.Global.ZEN_MODE)*/ ) {
                updateAll()
            }
        }
    }

    private fun startForeground() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(NotificationChannel("recorder",
                    resources.getString(R.string.ringer_widget_title), NotificationManager.IMPORTANCE_LOW))
        }

        startForeground(1537,
                NotificationCompat.Builder(this, "recorder")
                        .setSmallIcon(R.mipmap.ic_launcher_boredsigns)
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .build())
    }

    private fun updateAll() {
        Utils.sendWidgetUpdate(this, RecorderWidget::class.java, null)
    }

    private fun record() {
        recorder.record()
    }

    private fun play() {
        recorder.play()
    }

    private fun stop() {
        recorder.stop()
    }
}
