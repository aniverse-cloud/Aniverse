package com.example.dialer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Call
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
import androidx.navigation.NavController

@Composable
fun DialerScreen(navController: NavController? = null) {
    var phoneNumber by remember { mutableStateOf("") }
    val context = LocalContext.current

    val callPhonePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            placeCall(context, phoneNumber)
        } else {
            Toast.makeText(context, "Permission required to place calls.", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display area for the typed number
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = phoneNumber,
                fontSize = 36.sp,
                maxLines = 1,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Keypad grid
        val keys = listOf(
            listOf("1" to "", "2" to "ABC", "3" to "DEF"),
            listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
            listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
            listOf("*" to "", "0" to "+", "#" to "")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            for (row in keys) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    for ((number, letters) in row) {
                        DialerKey(number, letters) {
                            phoneNumber += it
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Bottom action row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(48.dp)) // Placeholder for symmetry

            // Call Button
            FloatingActionButton(
                onClick = {
                    if (phoneNumber.isNotEmpty()) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                            placeCall(context, phoneNumber)
                        } else {
                            callPhonePermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = "Call",
                    modifier = Modifier.size(36.dp)
                )
            }

            // Backspace Button
            IconButton(
                onClick = {
                    if (phoneNumber.isNotEmpty()) {
                        phoneNumber = phoneNumber.dropLast(1)
                    }
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = "Backspace",
                    tint = if (phoneNumber.isEmpty()) Color.LightGray else Color.Gray
                )
            }
        }
    }
}

private fun placeCall(context: Context, phoneNumber: String) {
    try {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val uri = Uri.fromParts("tel", phoneNumber, null)
        telecomManager.placeCall(uri, null)
    } catch (e: SecurityException) {
        // Permission not granted or not default dialer
        Toast.makeText(context, "Cannot place call. Set as default dialer?", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun DialerKey(number: String, letters: String, onClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color(0xFFF0F0F0))
            .clickable { onClick(number) },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = number,
                fontSize = 28.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
