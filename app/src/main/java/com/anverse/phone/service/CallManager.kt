package com.anverse.phone.service

import android.telecom.Call
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CallManager {
    private val _activeCalls = MutableStateFlow<List<Call>>(emptyList())
    val activeCalls: StateFlow<List<Call>> = _activeCalls.asStateFlow()

    fun addCall(call: Call) {
        val current = _activeCalls.value.toMutableList()
        if (!current.contains(call)) {
            current.add(call)
            _activeCalls.value = current
        }
    }

    fun removeCall(call: Call) {
        val current = _activeCalls.value.toMutableList()
        current.remove(call)
        _activeCalls.value = current
    }

    fun mergeCalls() {
        val calls = _activeCalls.value
        if (calls.size >= 2) {
            calls[0].conference(calls[1])
        }
    }

    fun disconnectAll() {
        _activeCalls.value.forEach { it.disconnect() }
    }
}
