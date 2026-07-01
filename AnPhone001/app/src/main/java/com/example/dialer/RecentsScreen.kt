package com.example.dialer

import android.Manifest
import android.content.ContentResolver
import android.provider.CallLog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.text.SimpleDateFormat
import java.util.*

data class RecentCall(val number: String, val date: String, val duration: String, val type: Int)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RecentsScreen() {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.READ_CALL_LOG)
    var calls by remember { mutableStateOf<List<RecentCall>>(emptyList()) }

    LaunchedEffect(permissionState.status) {
        if (permissionState.status.isGranted) {
            val contentResolver: ContentResolver = context.contentResolver
            val cursor = contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.DURATION, CallLog.Calls.TYPE),
                null, null, CallLog.Calls.DATE + " DESC"
            )
            val fetchedCalls = mutableListOf<RecentCall>()
            cursor?.use {
                val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
                val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)
                val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)

                while (it.moveToNext() && fetchedCalls.size < 50) {
                    val number = it.getString(numberIndex)
                    val dateMillis = it.getLong(dateIndex)
                    val duration = it.getString(durationIndex)
                    val type = it.getInt(typeIndex)

                    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                    val dateString = formatter.format(Date(dateMillis))

                    fetchedCalls.add(RecentCall(number ?: "Unknown", dateString, duration ?: "0", type))
                }
            }
            calls = fetchedCalls
        }
    }

    if (!permissionState.status.isGranted) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
            Text("Call Log permission is required to view recents.")
            Button(onClick = { permissionState.launchPermissionRequest() }, modifier = Modifier.padding(top = 16.dp)) {
                Text("Grant Permission")
            }
        }
        return
    }

    if (calls.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No recent calls",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(calls) { call ->
                ListItem(
                    headlineContent = { Text(call.number, fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("${call.date} • ${getCallTypeString(call.type)}") },
                )
                Divider()
            }
        }
    }
}

fun getCallTypeString(type: Int): String {
    return when (type) {
        CallLog.Calls.INCOMING_TYPE -> "Incoming"
        CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
        CallLog.Calls.MISSED_TYPE -> "Missed"
        CallLog.Calls.REJECTED_TYPE -> "Rejected"
        else -> "Unknown"
    }
}
