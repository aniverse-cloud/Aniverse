package com.anverse.phone.ui.screens

import android.os.Bundle
import android.telecom.Call
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anverse.phone.service.CallManager
import com.anverse.phone.ui.theme.AnverseTheme

class ActiveCallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AnverseTheme {
                val activeCalls by CallManager.activeCalls.collectAsState()

                LaunchedEffect(activeCalls) {
                    if (activeCalls.isEmpty()) finish()
                }

                if (activeCalls.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF3F4F6))
                            .padding(vertical = 48.dp, horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        val primaryCall = activeCalls.first()
                        val number = primaryCall.details?.handle?.schemeSpecificPart ?: "Unknown"

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Contact",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$number",
                                fontSize = 16.sp,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            val stateText = if (primaryCall.state == Call.STATE_ACTIVE) "Active" else "Ringing"
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(color = Color.DarkGray, shape = CircleShape, modifier = Modifier.padding(end = 4.dp)) {
                                    Text(" VoLTE ", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(2.dp))
                                }
                                Text("SIM 1 $stateText", color = Color.DarkGray, fontSize = 14.sp)
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                CallControlButton(Icons.Default.Note, "Notes", false) {}
                                CallControlButton(Icons.Default.Add, "Add call", false) { }
                                CallControlButton(Icons.Default.MicOff, "Mute", false) {}
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                CallControlButton(Icons.Default.Voicemail, "Record", false) {}
                                CallControlButton(Icons.Default.People, "Contacts", false) {}
                                CallControlButton(Icons.Default.Pause, "Hold", false) {}
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { }) {
                                Icon(Icons.Default.Dialpad, contentDescription = "Keypad", tint = Color.Black, modifier = Modifier.size(32.dp))
                            }

                            FloatingActionButton(
                                onClick = { CallManager.disconnectAll() },
                                containerColor = MaterialTheme.colorScheme.error,
                                shape = CircleShape,
                                modifier = Modifier.size(72.dp)
                            ) {
                                Icon(Icons.Default.CallEnd, contentDescription = "End Call", tint = Color.White, modifier = Modifier.size(36.dp))
                            }

                            IconButton(onClick = { }) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Speaker", tint = Color.Black, modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun CallControlButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isActive: Boolean, onClick: () -> Unit) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button
            ) { onClick() }
        ) {
            Surface(
                shape = CircleShape,
                color = if (isActive) MaterialTheme.colorScheme.primary else Color(0xFFE5E7EB),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = if (isActive) Color.White else Color.DarkGray,
                    modifier = Modifier.padding(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 14.sp, color = Color.DarkGray)
        }
    }
}