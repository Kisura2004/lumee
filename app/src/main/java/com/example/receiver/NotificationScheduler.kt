package com.example.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

object NotificationScheduler {
    private fun scheduleAlarm(context: Context, requestCode: Int, timeStr: String, typeStr: String) {
        val parts = timeStr.split(":")
        if (parts.size != 2) return
        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("NOTIFICATION_TYPE", typeStr)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            try {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } catch (ex: Exception) {
                // Fail silently/gracefully on constrained devices
            }
        }
    }

    private fun cancelAlarm(context: Context, requestCode: Int, typeStr: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("NOTIFICATION_TYPE", typeStr)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleNextNotification(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val profile = db.lumeeDao().getUserProfile()
            val preferredReminderTime = profile?.preferredReminderTime

            val prefs = context.getSharedPreferences("lumee_prefs", Context.MODE_PRIVATE)
            val isMorningEnabled = prefs.getBoolean("morning_wakeup_enabled", true)
            val morningTime = prefs.getString("morning_wakeup_time", "07:00") ?: "07:00"
            val isEveningEnabled = prefs.getBoolean("evening_motivation_enabled", true)
            val eveningTime = prefs.getString("evening_motivation_time", "21:30") ?: "21:30"

            // 1. Preferred Reminder
            if (preferredReminderTime != null) {
                scheduleAlarm(context, 1001, preferredReminderTime, "PREFERRED")
            } else {
                cancelAlarm(context, 1001, "PREFERRED")
            }

            // 2. Morning Wakeup
            if (isMorningEnabled) {
                scheduleAlarm(context, 1002, morningTime, "MORNING")
            } else {
                cancelAlarm(context, 1002, "MORNING")
            }

            // 3. Evening Motivation
            if (isEveningEnabled) {
                scheduleAlarm(context, 1003, eveningTime, "EVENING")
            } else {
                cancelAlarm(context, 1003, "EVENING")
            }
        }
    }

    fun cancelNotification(context: Context) {
        cancelAlarm(context, 1001, "PREFERRED")
        cancelAlarm(context, 1002, "MORNING")
        cancelAlarm(context, 1003, "EVENING")
    }
}
