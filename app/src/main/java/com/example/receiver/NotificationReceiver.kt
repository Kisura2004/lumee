package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.GreetingProvider

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "lumee_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Lumee Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Gentle daily checkups and motivation quotes"
            }
            manager.createNotificationChannel(channel)
        }

        val type = intent.getStringExtra("NOTIFICATION_TYPE") ?: "PREFERRED"

        // Create default content intent to open standard MainActivity
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 1. Action: Breathe/Inhale PendingIntent
        val breatheIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NAVIGATE_TO", "TODAY")
            putExtra("TRIGGER_BREATHING", true)
            if (type == "EVENING") {
                putExtra("BREATHING_TECHNIQUE", "CALM")
            } else {
                putExtra("BREATHING_TECHNIQUE", "BOX")
            }
        }
        val breathePendingIntent = PendingIntent.getActivity(
            context,
            1,
            breatheIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Action: Speak/Reflect PendingIntent
        val reflectIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NAVIGATE_TO", "REFLECTIONS")
            putExtra("TRIGGER_COMPOSER", true)
        }
        val reflectPendingIntent = PendingIntent.getActivity(
            context,
            2,
            reflectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Action: Read Quotes PendingIntent
        val quotesIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NAVIGATE_TO", "QUOTES")
        }
        val quotesPendingIntent = PendingIntent.getActivity(
            context,
            3,
            quotesIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        when (type) {
            "MORNING" -> {
                val greeting = try {
                    GreetingProvider.morningGreetings.random().text
                } catch (e: Exception) {
                    "Today is a playground of possibilities. Let your curiosity lead the way."
                }
                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Morning Wakeup 🌅")
                    .setContentText(greeting)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(greeting))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(contentPendingIntent)
                    .setAutoCancel(true)
                    .addAction(android.R.drawable.ic_media_play, "Quick Inhale 🌸", breathePendingIntent)
                    .addAction(android.R.drawable.ic_menu_edit, "Morning Log ✍️", reflectPendingIntent)
                    .build()
                manager.notify(1002, notification)
            }
            "EVENING" -> {
                val greeting = try {
                    val scrambledIdx = java.util.Random().nextInt(GreetingProvider.TOTAL_GENERATIVE_QUOTES)
                    GreetingProvider.getQuoteAt(scrambledIdx).text
                } catch (e: Exception) {
                    "Your inner heart quietly restores the weights of today. Rest fully."
                }
                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Late Night Motivation 🌌")
                    .setContentText(greeting)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(greeting))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(contentPendingIntent)
                    .setAutoCancel(true)
                    .addAction(android.R.drawable.ic_media_play, "Unwind Calm 🧘", breathePendingIntent)
                    .addAction(android.R.drawable.ic_menu_help, "Quotes Sanctuary 📖", quotesPendingIntent)
                    .build()
                manager.notify(1003, notification)
            }
            else -> { // "PREFERRED"
                val greeting = try {
                    GreetingProvider.afternoonGreetings.random()
                } catch (e: Exception) {
                    "Take a slow breath. Relax your shoulders. You are doing well."
                }
                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Gentle Moment from Lumee ☀️")
                    .setContentText(greeting)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(greeting))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(contentPendingIntent)
                    .setAutoCancel(true)
                    .addAction(android.R.drawable.ic_media_play, "Breathe", breathePendingIntent)
                    .addAction(android.R.drawable.ic_menu_edit, "Journal", reflectPendingIntent)
                    .build()
                manager.notify(1001, notification)
            }
        }

        // Reschedule alarm for next day
        NotificationScheduler.scheduleNextNotification(context)
    }
}
