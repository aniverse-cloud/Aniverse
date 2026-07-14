package com.anverse.phone.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import android.telecom.TelecomManager
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.anverse.phone.viewmodel.CallLogItem
import com.anverse.phone.viewmodel.DialerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardLayout(viewModel: DialerViewModel) {
    var selectedBottomTab by remember { mutableStateOf(0) }
    var isDialpadOpen by remember { mutableStateOf(false) }
    var inputNumber by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                NavigationBarItem(
                    selected = selectedBottomTab == 0,
                    onClick = { selectedBottomTab = 0; isDialpadOpen = false },
                    icon = { Icon(if (selectedBottomTab == 0) Icons.Filled.Phone else Icons.Outlined.Phone, "Dial") },
                    label = { Text("Dial") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, selectedTextColor = MaterialTheme.colorScheme.primary)
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 1,
                    onClick = { selectedBottomTab = 1; isDialpadOpen = false },
                    icon = { Icon(if (selectedBottomTab == 1) Icons.Filled.People else Icons.Outlined.People, "Contacts") },
                    label = { Text("Contacts") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, selectedTextColor = MaterialTheme.colorScheme.primary)
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 2,
                    onClick = { selectedBottomTab = 2; isDialpadOpen = false },
                    icon = { Icon(Icons.Outlined.StarOutline, "Favorites") },
                    label = { Text("Favorites") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, selectedTextColor = MaterialTheme.colorScheme.primary)
                )
            }
        },
        floatingActionButton = {
            if (!isDialpadOpen) {
                if (selectedBottomTab == 0) {
                    FloatingActionButton(
                        onClick = { isDialpadOpen = true },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Dialpad, contentDescription = "Open Dialpad", tint = Color.White)
                    }
                } else if (selectedBottomTab == 1) {
                    FloatingActionButton(
                        onClick = { },
                        containerColor = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Contact", tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            when (selectedBottomTab) {
                0 -> DialTabView(viewModel)
                1 -> ContactsTabView(viewModel)
                2 -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Favorites Empty") }
            }

            AnimatedVisibility(
                visible = isDialpadOpen,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                DialpadOverlay(
                    number = inputNumber,
                    onValueChange = { inputNumber = it },
                    onClose = { isDialpadOpen = false }
                )
            }
        }
    }
}

@Composable
fun DialTabView(viewModel: DialerViewModel) {
    var selectedTopTab by remember { mutableStateOf(0) }
    val allLogs by viewModel.callLogs.collectAsState()

    val displayLogs = if (selectedTopTab == 1) {
        allLogs.filter { it.type == CallLog.Calls.MISSED_TYPE }
    } else {
        allLogs
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dial", fontSize = 36.sp, fontWeight = FontWeight.Normal)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Default.Checklist, contentDescription = "Select")
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
        }

        TabRow(
            selectedTabIndex = selectedTopTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Tab(
                selected = selectedTopTab == 0,
                onClick = { selectedTopTab = 0 },
                text = { Text("All", fontWeight = if(selectedTopTab == 0) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = selectedTopTab == 1,
                onClick = { selectedTopTab = 1 },
                text = { Text("Missed", fontWeight = if(selectedTopTab == 1) FontWeight.Bold else FontWeight.Normal) }
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            items(displayLogs) { log ->
                val isMissed = log.type == CallLog.Calls.MISSED_TYPE
                val textColor = if (isMissed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isMissed) {
                            Icon(Icons.Default.PhoneMissed, contentDescription = "Missed", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                        }
                        Column {
                            Text(log.number, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = textColor)
                            Text("Unknown location", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(log.duration, color = Color.Gray, fontSize = 12.sp)
                        Spacer(Modifier.width(16.dp))
                        Icon(Icons.Outlined.Info, contentDescription = "Info", tint = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun ContactsTabView(viewModel: DialerViewModel) {
    val contacts by viewModel.contacts.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Contacts", fontSize = 36.sp, fontWeight = FontWeight.Normal)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Default.Checklist, contentDescription = "Select")
                Icon(Icons.Default.Search, contentDescription = "Search")
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            item {
                Row(modifier = Modifier.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, tint = Color.White, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Profile", fontSize = 16.sp)
                }
                Row(modifier = Modifier.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(Color(0xFF4ADE80), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.People, tint = Color.White, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Groups", fontSize = 16.sp)
                }
            }

            items(contacts) { contact ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(Color(0xFF93C5FD), CircleShape), contentAlignment = Alignment.Center) {
                        Text(if (contact.name.isNotEmpty()) contact.name.take(1) else "?", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(contact.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun DialpadOverlay(number: String, onValueChange: (String) -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FAFB), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(60.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = number, fontSize = 36.sp, fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(modifier = Modifier.height(16.dp))

        val keys = listOf(
            listOf(Pair("1", "oo"), Pair("2", "ABC"), Pair("3", "DEF")),
            listOf(Pair("4", "GHI"), Pair("5", "JKL"), Pair("6", "MNO")),
            listOf(Pair("7", "PQRS"), Pair("8", "TUV"), Pair("9", "WXYZ")),
            listOf(Pair("*", ""), Pair("0", "+"), Pair("#", ""))
        )

        keys.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { (digit, letters) ->
                    Column(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .clickable { onValueChange(number + digit) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(digit, fontSize = 32.sp, color = MaterialTheme.colorScheme.onBackground)
                        if (letters.isNotEmpty()) {
                            Text(letters, fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onClose() }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.Gray)
            }

            Button(
                onClick = { if (number.isNotEmpty()) placeSystemCall(context, number) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.height(50.dp).width(120.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("SIM 1", color = Color.White)
            }

            Button(
                onClick = { if (number.isNotEmpty()) placeSystemCall(context, number) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.height(50.dp).width(120.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("SIM 2", color = Color.White)
            }

            IconButton(onClick = { if (number.isNotEmpty()) onValueChange(number.dropLast(1)) }) {
                Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = Color.Gray)
            }
        }
    }
}

private fun placeSystemCall(context: Context, number: String) {
    val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    val uri = Uri.fromParts("tel", number, null)
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
        telecomManager.placeCall(uri, Bundle())
    }
}