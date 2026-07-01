package com.example.dialer

import android.telecom.Call
import android.telecom.InCallService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CallStateWrapper(
    val call: Call?,
    val state: Int = Call.STATE_NEW,
    val details: Call.Details? = null,
    val updateTick: Long = System.currentTimeMillis() // ensure distinct emissions
)

object CallManager {
    var inCallService: InCallService? = null

    fun setAudioRoute(route: Int) {
        inCallService?.setAudioRoute(route)
    }

    private val _callState = MutableStateFlow(CallStateWrapper(null))
    val callState: StateFlow<CallStateWrapper> = _callState.asStateFlow()

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            _callState.value = CallStateWrapper(call, state, call.details)
        }

        override fun onDetailsChanged(call: Call, details: Call.Details) {
            super.onDetailsChanged(call, details)
            _callState.value = CallStateWrapper(call, call.state, details)
        }
    }

    fun updateCall(call: Call?) {
        _callState.value.call?.unregisterCallback(callCallback)
        if (call == null) {
            _callState.value = CallStateWrapper(null, Call.STATE_DISCONNECTED, null)
        } else {
            call.registerCallback(callCallback)
            _callState.value = CallStateWrapper(call, call.state, call.details)
        }
    }
}
