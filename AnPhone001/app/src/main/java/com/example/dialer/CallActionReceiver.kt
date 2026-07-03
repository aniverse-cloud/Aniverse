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
                "ACTION_DECLINE_CALL" -> {
                    call.disconnect()
                }
            }
        }
    }
}
