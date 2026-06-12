package com.anverse.phone.service

import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService
import com.anverse.phone.ui.screens.ActiveCallActivity

class AnverseInCallService : InCallService() {

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            CallManager.updateCall(call)

            if (state == Call.STATE_DISCONNECTED) {
                call.unregisterCallback(this)
                CallManager.removeCall(call)
                if (CallManager.activeCalls.value.isEmpty()) {
                    stopSelf()
                }
            }
        }
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        call.registerCallback(callCallback)
        CallManager.addCall(call)

        val intent = Intent(this, ActiveCallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        call.unregisterCallback(callCallback)
        CallManager.removeCall(call)
    }
}
