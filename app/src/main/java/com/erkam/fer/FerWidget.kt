package com.erkam.fer

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class FerWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        Store.init(context)
        ids.forEach { manager.updateAppWidget(it, views(context)) }
    }

    companion object {
        private val faces = intArrayOf(R.drawable.dog0, R.drawable.dog1, R.drawable.dog2, R.drawable.dog3, R.drawable.dog4)

        fun views(context: Context): RemoteViews {
            val c = Store.count()
            val v = RemoteViews(context.packageName, R.layout.widget_fer)
            v.setImageViewResource(R.id.widget_face, faces[Store.stage(c, Store.limit)])
            v.setTextViewText(R.id.widget_count, c.toString())
            v.setTextViewText(R.id.widget_label, "/ ${Store.limit} reel bugün")
            val open = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            v.setOnClickPendingIntent(R.id.widget_root, open)
            return v
        }

        fun updateAll(context: Context) {
            Store.init(context)
            val m = AppWidgetManager.getInstance(context)
            val ids = m.getAppWidgetIds(ComponentName(context, FerWidget::class.java))
            if (ids.isEmpty()) return
            val v = views(context)
            ids.forEach { m.updateAppWidget(it, v) }
        }

        fun requestPin(context: Context): Boolean {
            val m = AppWidgetManager.getInstance(context)
            if (!m.isRequestPinAppWidgetSupported) return false
            return m.requestPinAppWidget(ComponentName(context, FerWidget::class.java), null, null)
        }
    }
}
