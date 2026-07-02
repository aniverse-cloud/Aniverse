package com.example.dialer

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DialerScreen(navController: NavController? = null) {
    var phoneNumber by remember { mutableStateOf("") }
    val context = LocalContext.current
    val callPermissionState = rememberPermissionState(Manifest.permission.CALL_PHONE)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = phoneNumber,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        val keys = listOf(
            "1" to "", "2" to "ABC", "3" to "DEF",
            "4" to "GHI", "5" to "JKL", "6" to "MNO",
            "7" to "PQRS", "8" to "TUV", "9" to "WXYZ",
            "*" to "", "0" to "+", "#" to ""
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(keys) { (number, letters) ->
                DialerKey(
                    number = number,
                    letters = letters,
                    onClick = { phoneNumber += number }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(48.dp))
            FloatingActionButton(
                onClick = {
                    if (phoneNumber.isNotEmpty()) {
                        if (callPermissionState.status.isGranted) {
                            val intent = Intent(Intent.ACTION_CALL).apply {
                                data = Uri.parse("tel:$phoneNumber")
                            }
                            context.startActivity(intent)
                        } else {
                            callPermissionState.launchPermissionRequest()
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Filled.Call, contentDescription = "Call", modifier = Modifier.size(32.dp))
            }
            IconButton(
                onClick = {
                    if (phoneNumber.isNotEmpty()) {
                        phoneNumber = phoneNumber.dropLast(1)
                    }
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Backspace")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun DialerKey(number: String, letters: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = number, fontSize = 28.sp, fontWeight = FontWeight.Medium)
            if (letters.isNotEmpty()) {
                Text(text = letters, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
