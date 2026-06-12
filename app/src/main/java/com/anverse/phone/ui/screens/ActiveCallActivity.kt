package com.anverse.phone.ui.screens

import android.net.Uri
import android.os.Bundle
import android.telecom.Call
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anverse.phone.service.CallManager
import com.anverse.phone.ui.theme.AnverseTheme
import kotlinx.coroutines.flow.map

class ActiveCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnverseTheme {
                val calls by CallManager.activeCalls.collectAsState()
                val primaryCall = calls.firstOrNull()

                if (primaryCall == null) {
                    finish()
                    return@AnverseTheme
                }

                ActiveCallScreen(primaryCall)
            }
        }
    }
}

@Composable
fun ActiveCallScreen(call: Call) {
    // Recompose when state changes
    val callState = call.details.state
    val handle: Uri? = call.details?.handle
    val number = handle?.schemeSpecificPart ?: "Unknown Caller"

    val stateText = when (callState) {
        Call.STATE_RINGING -> "Incoming Call"
        Call.STATE_DIALING -> "Dialing..."
        Call.STATE_ACTIVE -> "Ongoing Call"
        Call.STATE_HOLDING -> "On Hold"
        Call.STATE_DISCONNECTED -> "Call Ended"
        else -> "Connecting..."
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 48.dp)
            ) {
                Text(
                    text = stateText,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = number,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (callState == Call.STATE_RINGING) {
                    IconButton(
                        onClick = { CallManager.answerCall(call) },
                        modifier = Modifier.size(72.dp).background(Color(0xFF00E676), CircleShape)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Answer", tint = Color.Black, modifier = Modifier.size(32.dp))
                    }
                    IconButton(
                        onClick = { CallManager.rejectCall(call) },
                        modifier = Modifier.size(72.dp).background(Color(0xFFFF1744), CircleShape)
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "Decline", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                } else {
                    IconButton(
                        onClick = { CallManager.disconnectCall(call) },
                        modifier = Modifier.size(72.dp).background(Color(0xFFFF1744), CircleShape)
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "End Call", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}
