package com.example.dialer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun CallScreen(navController: NavController, phoneNumber: String = "62906 72442") {
    // In a real app, you would pass the name and number, and manage the call state.
    val contactName = "Raju Prasad Bhagat"

    // States for toggles
    var isMuted by remember { mutableStateOf(false) }
    var isOnHold by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF2F3F5) // Light gray background matching screenshot
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Caller Info
            Text(
                text = contactName,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = phoneNumber,
                fontSize = 18.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color.DarkGray,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = "VoLTE",
                        fontSize = 10.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "SIM 1 Dialing",
                    fontSize = 16.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CallActionButton(
                        icon = Icons.Filled.MoreVert, // Notes (placeholder)
                        label = "Notes",
                        onClick = { /* TODO */ }
                    )
                    CallActionButton(
                        icon = Icons.Filled.Add,
                        label = "Add call",
                        onClick = { /* TODO */ }
                    )
                    CallActionButton(
                        icon = Icons.Filled.Settings, // Mute (placeholder)
                        label = "Mute",
                        isActive = isMuted,
                        onClick = { isMuted = !isMuted }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CallActionButton(
                        icon = Icons.Filled.Star, // Record (placeholder)
                        label = "Record",
                        isActive = isRecording,
                        onClick = { isRecording = !isRecording }
                    )
                    CallActionButton(
                        icon = Icons.Filled.Person, // Contacts
                        label = "Contacts",
                        onClick = { /* TODO */ }
                    )
                    CallActionButton(
                        icon = Icons.Filled.Close, // Hold (placeholder)
                        label = "Hold",
                        isActive = isOnHold,
                        onClick = { isOnHold = !isOnHold }
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Bottom Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Keypad
                IconButton(onClick = { /* TODO */ }) {
                    Icon(
                        imageVector = Icons.Filled.Phone, // Keypad (placeholder)
                        contentDescription = "Keypad",
                        modifier = Modifier.size(32.dp),
                        tint = Color.DarkGray
                    )
                }

                // End Call
                FloatingActionButton(
                    onClick = { navController.popBackStack() },
                    containerColor = Color(0xFFEA4335), // Red
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close, // End Call (placeholder)
                        contentDescription = "End call",
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Speaker
                IconButton(onClick = { isSpeakerOn = !isSpeakerOn }) {
                    Icon(
                        imageVector = Icons.Filled.Call, // Speaker (placeholder)
                        contentDescription = "Speaker",
                        modifier = Modifier.size(32.dp),
                        tint = if (isSpeakerOn) MaterialTheme.colorScheme.primary else Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun CallActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (isActive) Color.LightGray else Color.Transparent)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.Black else Color.DarkGray,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.DarkGray
        )
    }
}
