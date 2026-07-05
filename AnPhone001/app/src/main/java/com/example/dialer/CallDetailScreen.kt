package com.example.dialer

import android.content.Intent
import android.net.Uri
import android.provider.CallLog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallDetailScreen(
    navController: NavController,
    phoneNumber: String,
    contactName: String,
    recentCalls: List<ContactOrCall>
) {
    val context = LocalContext.current
    var showAllLogs by remember { mutableStateOf(false) }

    // Find the specific contact
    val contact = recentCalls.find { it.number == phoneNumber }
    val logs = contact?.logs ?: emptyList()

    val displayLogs = if (showAllLogs) logs else logs.take(3)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name
            val displayName = if (contactName.isNotEmpty() && contactName != "null") contactName else phoneNumber
            Text(
                text = displayName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(
                    icon = Icons.Filled.Message,
                    label = "Message",
                    color = Color(0xFF4285F4),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phoneNumber, null))
                        context.startActivity(intent)
                    }
                )
                ActionButton(
                    icon = Icons.Filled.Call,
                    label = "SIM 1",
                    color = Color(0xFF34A853),
                    onClick = {
                        val intent = Intent(Intent.ACTION_CALL).apply {
                            data = Uri.parse("tel:$phoneNumber")
                        }
                        context.startActivity(intent)
                    }
                )
                ActionButton(
                    icon = Icons.Filled.Call,
                    label = "SIM 2",
                    color = Color(0xFF34A853),
                    onClick = {
                        val intent = Intent(Intent.ACTION_CALL).apply {
                            data = Uri.parse("tel:$phoneNumber")
                        }
                        context.startActivity(intent)
                    }
                )
                ActionButton(
                    icon = Icons.Filled.VideoCall,
                    label = "Video",
                    color = Color(0xFF34A853),
                    onClick = { } // Placeholder for video call
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Phone Number Details
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = phoneNumber,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Mobile", // Placeholder for location/type
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // Call Logs Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Call logs",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                if (logs.size > 3 && !showAllLogs) {
                    Text(
                        text = "More >",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { showAllLogs = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(displayLogs) { log ->
                    CallLogItem(log)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun ActionButton(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CallLogItem(log: CallLogEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Placeholder for SIM icon
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.LightGray, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("1", fontSize = 10.sp, color = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = getCallTypeString(log.type),
                    fontSize = 16.sp
                )
            }
        }

        val dateText = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault()).format(Date(log.dateMillis))
        Text(
            text = dateText,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

@Composable
fun CallDetailScreenWrapper(navController: NavController, number: String, name: String) {
    val context = LocalContext.current
    var logs by remember { mutableStateOf<List<CallLogEntry>>(emptyList()) }

    LaunchedEffect(number) {
        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.TYPE),
            "${CallLog.Calls.NUMBER} = ?",
            arrayOf(number),
            "${CallLog.Calls.DATE} DESC"
        )

        val fetchedLogs = mutableListOf<CallLogEntry>()
        cursor?.use {
            val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
            val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)

            while (it.moveToNext()) {
                val dateMillis = it.getLong(dateIndex)
                val type = it.getInt(typeIndex)
                val dateString = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(dateMillis))
                fetchedLogs.add(CallLogEntry(dateString, type, dateMillis))
            }
        }
        logs = fetchedLogs
    }

    // Adapt to CallDetailScreen format
    val mockRecentCalls = listOf(ContactOrCall(name, number, logs = logs))
    CallDetailScreen(navController, number, name, mockRecentCalls)
}
