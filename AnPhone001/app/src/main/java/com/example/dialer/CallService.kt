package com.example.dialer

import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService

class CallService : InCallService() {
    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            if (state == Call.STATE_DISCONNECTED) {
                NotificationHelper.cancelNotification(this@CallService)
            } else {
                NotificationHelper.showCallNotification(this@CallService, call)
            }
        }
    }
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        CallManager.inCallService = this
        CallManager.updateCall(call)
        call.registerCallback(callCallback)

        NotificationHelper.showCallNotification(this, call)

        if (call.state != Call.STATE_RINGING) {
            // Launch UI if not already in foreground for outgoing calls or active calls
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("SHOW_CALL_SCREEN", true)
            }
            startActivity(intent)
        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        if (CallManager.callState.value.call == call) {
            CallManager.updateCall(null)
            call.unregisterCallback(callCallback)
            NotificationHelper.cancelNotification(this)
        }
    }
}
