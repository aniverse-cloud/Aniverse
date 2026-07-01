package com.example.dialer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager

class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val call = CallManager.callState.value.call

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1001)

        if (call != null) {
            when (action) {
                "ACTION_ANSWER_CALL" -> {
                    call.answer(call.details.videoState)
                    val callIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("SHOW_CALL_SCREEN", true)
                    }
                    context.startActivity(callIntent)
                }
                "ACTION_DECLINE_CALL" -> {
                    call.disconnect()
                }
            }
        }
    }
}
