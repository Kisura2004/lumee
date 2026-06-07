package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.data.GreetingProvider
import com.example.data.database.AppDatabase
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class LumeeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val database = AppDatabase.getDatabase(context)
        val dao = database.lumeeDao()

        CoroutineScope(Dispatchers.IO).launch {
            val profile = dao.getUserProfile()
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

            val text = if (currentHour >= 11) {
                GreetingProvider.afternoonGreetings.randomOrNull() ?: "The day is still yours."
            } else {
                "Good morning. A new day has opened for you."
            }

            val streakText = if (profile != null && profile.streakCount > 0) {
                "✨ Morning Streak: ${profile.streakCount}"
            } else {
                "🌱 Begin gently today"
            }

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.lumee_widget_layout)
                
                views.setTextViewText(R.id.widget_greeting, "“$text”")
                views.setTextViewText(R.id.widget_streak, streakText)

                // Intent to click and launch the main application
                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_greeting, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
