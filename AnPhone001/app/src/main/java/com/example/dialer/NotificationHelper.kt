package com.example.dialer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import android.telecom.Call

object NotificationHelper {
    private const val CHANNEL_ID = "incoming_calls_channel"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Incoming Calls"
            val descriptionText = "Notifications for incoming calls"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showCallNotification(context: Context, call: Call) {
        createNotificationChannel(context)

        val callerName = "Incoming Call"
        val callerNumber = call.details.handle?.schemeSpecificPart ?: "Unknown Number"

        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("SHOW_CALL_SCREEN", true)
            action = "ACTION_SHOW_CALL_SCREEN"
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Tapping the notification body should do the same as full screen intent
        val contentIntent = fullScreenPendingIntent

        // Action Answer: Launch MainActivity directly instead of going through a receiver
        val answerIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            action = "ACTION_ANSWER_CALL"
            putExtra("SHOW_CALL_SCREEN", true)
        }
        val answerPendingIntent = PendingIntent.getActivity(
            context,
            1,
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val declineIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = "ACTION_DECLINE_CALL"
        }
        val declinePendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isRinging = call.state == Call.STATE_RINGING
        val isOngoing = call.state == Call.STATE_ACTIVE || call.state == Call.STATE_DIALING

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_action_call)
            .setContentTitle(if (isOngoing) "Ongoing Call" else callerName)
            .setContentText(callerNumber)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(false)
            .setOngoing(true)
            .setColorized(true)
            .setContentIntent(contentIntent)

        if (isRinging) {
            builder.setColor(android.graphics.Color.parseColor("#FF9800")) // Orange
            builder.setFullScreenIntent(fullScreenPendingIntent, true)
            builder.addAction(android.R.drawable.ic_menu_call, "Answer", answerPendingIntent)
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Decline", declinePendingIntent)
        } else {
            builder.setColor(android.graphics.Color.parseColor("#4CAF50")) // Green
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())

    }

    fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
