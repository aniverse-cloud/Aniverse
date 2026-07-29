package com.example.dialer

import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen() {
    val context = LocalContext.current
    var isTransferEnabled by remember { mutableStateOf(false) }

    // Remote connection state
    var remoteApiName by remember { mutableStateOf("") }
    var remoteApiKey by remember { mutableStateOf("") }
    var isConnectedToRemote by remember { mutableStateOf(false) }

    // Generate local credentials
    val localApiName = remember { "anphone${Random.nextInt(100000, 999999)}" }
    val localApiKey = remember { "key_${Random.nextInt(1000, 9999)}" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Call Transfer (SIM Sharing)",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp, top = 48.dp)
        )

        // Section 1: Host SIM (User A)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Host Local SIM", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = "Allow another device to make calls using this phone's SIM card.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Enable Transfer")
                    Switch(
                        checked = isTransferEnabled,
                        onCheckedChange = {
                            isTransferEnabled = it
                            val intent = Intent(context, SimTransferService::class.java)
                            if (it) {
                                intent.action = SimTransferService.ACTION_START_HOST
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    context.startForegroundService(intent)
                                } else {
                                    context.startService(intent)
                                }
                            } else {
                                intent.action = SimTransferService.ACTION_STOP
                                context.startService(intent)
                            }
                        }
                    )
                }

                if (isTransferEnabled) {
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    Text("Share these credentials with the other device:", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("API Name: $localApiName", color = MaterialTheme.colorScheme.primary)
                    Text("API Key: $localApiKey", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Section 2: Use Remote SIM (User B)
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Use Remote SIM", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = "Connect to another device to make calls using its SIM card.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = remoteApiName,
                    onValueChange = { remoteApiName = it },
                    label = { Text("Remote API Name (IP Address)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = remoteApiKey,
                    onValueChange = { remoteApiKey = it },
                    label = { Text("Remote API Key") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )

                Button(
                    onClick = {
                        isConnectedToRemote = !isConnectedToRemote
                        val intent = Intent(context, SimTransferService::class.java)
                        if (isConnectedToRemote) {
                            intent.action = SimTransferService.ACTION_START_CLIENT
                            intent.putExtra(SimTransferService.EXTRA_IP, remoteApiName) // Using Name as IP for prototype
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                context.startForegroundService(intent)
                            } else {
                                context.startService(intent)
                            }
                        } else {
                            intent.action = SimTransferService.ACTION_STOP
                            context.startService(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConnectedToRemote) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (isConnectedToRemote) "Disconnect" else "Connect to SIM")
                }
            }
        }
    }
}
