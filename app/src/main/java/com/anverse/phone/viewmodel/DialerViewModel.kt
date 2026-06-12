package com.anverse.phone.viewmodel

import android.app.Application
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.provider.ContactsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ContactItem(val id: String, val name: String, val number: String)
data class CallLogItem(val number: String, val date: Long, val type: Int, val duration: String)

class DialerViewModel(application: Application) : AndroidViewModel(application) {
    private val _contacts = MutableStateFlow<List<ContactItem>>(emptyList())
    val contacts: StateFlow<List<ContactItem>> = _contacts

    private val _callLogs = MutableStateFlow<List<CallLogItem>>(emptyList())
    val callLogs: StateFlow<List<CallLogItem>> = _callLogs

    private val observerHandler = Handler(Looper.getMainLooper())

    private val contactsObserver = object : ContentObserver(observerHandler) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            fetchContacts()
        }
    }

    private val callLogsObserver = object : ContentObserver(observerHandler) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            fetchCallLogs()
        }
    }

    init {
        getApplication<Application>().contentResolver.registerContentObserver(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, true, contactsObserver
        )
        getApplication<Application>().contentResolver.registerContentObserver(
            CallLog.Calls.CONTENT_URI, true, callLogsObserver
        )
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().contentResolver.unregisterContentObserver(contactsObserver)
        getApplication<Application>().contentResolver.unregisterContentObserver(callLogsObserver)
    }

    fun fetchContacts() {
        viewModelScope.launch {
            try {
                val list = mutableListOf<ContactItem>()
                val cursor = getApplication<Application>().contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )
                cursor?.use {
                    val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)
                    while (it.moveToNext()) {
                        list.add(ContactItem(it.getString(idIndex), it.getString(nameIndex), it.getString(numIndex)))
                    }
                }
                _contacts.value = list
            } catch (e: SecurityException) {
                // Permissions might not be granted yet
                e.printStackTrace()
            }
        }
    }

    fun fetchCallLogs() {
        viewModelScope.launch {
            try {
                val list = mutableListOf<CallLogItem>()
                val cursor = getApplication<Application>().contentResolver.query(
                    CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC"
                )
                cursor?.use {
                    val numIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                    val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
                    val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
                    val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)
                    while (it.moveToNext()) {
                        list.add(CallLogItem(
                            it.getString(numIndex) ?: "Unknown",
                            it.getLong(dateIndex),
                            it.getInt(typeIndex),
                            it.getString(durationIndex) + "s"
                        ))
                    }
                }
                _callLogs.value = list
            } catch (e: SecurityException) {
                // Permissions might not be granted yet
                e.printStackTrace()
            }
        }
    }
}
