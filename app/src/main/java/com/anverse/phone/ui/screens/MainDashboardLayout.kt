package com.anverse.phone.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telecom.TelecomManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.anverse.phone.viewmodel.DialerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardLayout(viewModel: DialerViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    var inputNumber by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anverse Phone", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.secondary) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Logs") },
                    label = { Text("Recents") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Dialpad, contentDescription = "Keypad") },
                    label = { Text("Keypad") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Contacts") },
                    label = { Text("Contacts") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            AnimatedContent(targetState = selectedTab, label = "TabTransition") { targetTab ->
                when (targetTab) {
                    0 -> CallLogsView(viewModel)
                    1 -> ModernKeypadView(inputNumber, onValueChange = { inputNumber = it })
                    2 -> ContactsListView(viewModel)
                }
            }
        }
    }
}

@Composable
fun ModernKeypadView(number: String, onValueChange: (String) -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = number,
            fontSize = 36.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier.height(60.dp).padding(top = 10.dp)
        )

        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("*", "0", "#")
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            keys.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    row.forEach { digit ->
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary)
                                .clickable { onValueChange(number + digit) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(digit, fontSize = 28.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        IconButton(
            onClick = {
                if (number.isNotEmpty()) {
                    placeSystemCall(context, number)
                    onValueChange("")
                }
            },
            modifier = Modifier.size(72.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
        ) {
            Icon(Icons.Default.Call, contentDescription = "Dial", tint = Color.Black, modifier = Modifier.size(32.dp))
        }
    }
}

private fun placeSystemCall(context: Context, number: String) {
    val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    val uri = Uri.fromParts("tel", number, null)
    val extras = Bundle().apply {
        putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, false)
    }

    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
        telecomManager.placeCall(uri, extras)
    }
}

@Composable
fun CallLogsView(viewModel: DialerViewModel) {
    val callLogs by viewModel.callLogs.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(callLogs) { log ->
            val dateString = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(log.date))
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = log.number, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = dateString, fontSize = 14.sp, color = Color.Gray)
                    Text(text = "Duration: ${log.duration}", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun ContactsListView(viewModel: DialerViewModel) {
    val contacts by viewModel.contacts.collectAsState()
    val context = LocalContext.current

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(contacts) { contact ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { placeSystemCall(context, contact.number) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = contact.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = contact.number, fontSize = 16.sp, color = Color.Gray)
                }
            }
        }
    }
}
