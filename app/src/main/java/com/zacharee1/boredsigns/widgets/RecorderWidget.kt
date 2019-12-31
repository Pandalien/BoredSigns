package com.zacharee1.boredsigns.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.widget.RemoteViews
import com.zacharee1.boredsigns.R
import com.zacharee1.boredsigns.activities.PermissionsActivity
import com.zacharee1.boredsigns.services.RecorderService

class RecorderWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (perm in PermissionsActivity.RECORDER_REQUEST) {
            if (context.checkCallingOrSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(context, PermissionsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("class", this::class.java)
                context.startActivity(intent)
                return
            }
        }

        ContextCompat.startForegroundService(context, Intent(context, RecorderService::class.java))

        val views = RemoteViews(context.packageName, R.layout.recorder_widget)

        val recordIntent = PendingIntent.getBroadcast(context, 0, Intent(RecorderService.ACTION_RECORD), 0)
        val playIntent = PendingIntent.getBroadcast(context, 0, Intent(RecorderService.ACTION_PLAY), 0)
        val stopIntent = PendingIntent.getBroadcast(context, 0, Intent(RecorderService.ACTION_STOP), 0)

        views.setOnClickPendingIntent(R.id.record_btn, recordIntent)
        views.setOnClickPendingIntent(R.id.play_btn, playIntent)
        views.setOnClickPendingIntent(R.id.stop_btn, stopIntent)

        appWidgetManager.updateAppWidget(appWidgetIds, views)
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)

        context?.stopService(Intent(context, RecorderService::class.java))
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)

        context?.stopService(Intent(context, RecorderService::class.java))
    }


}

