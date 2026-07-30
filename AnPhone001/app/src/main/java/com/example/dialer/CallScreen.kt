package com.example.dialer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.content.ContentValues
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import java.io.IOException

@Composable
fun CallScreen(navController: NavController? = null) {
    val callStateWrapper by CallManager.callState.collectAsState()
    val call = callStateWrapper.call
    val state = callStateWrapper.state
    val context = LocalContext.current

    var isMuted by remember { mutableStateOf(false) }
    var isOnHold by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var mediaRecorder: MediaRecorder? by remember { mutableStateOf(null) }

    val callerName = callStateWrapper.details?.callerDisplayName ?: "Unknown Caller"
    val phoneNumber = callStateWrapper.details?.handle?.schemeSpecificPart ?: "Unknown Number"

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startRecording(context, callerName, phoneNumber, { mediaRecorder = it }, { isRecording = true }, {
                                    isSpeakerOn = true
                                    val callService = CallManager.callState.value.call
                                    if (callService != null) {
                                        val route = if (isSpeakerOn) android.telecom.CallAudioState.ROUTE_SPEAKER else android.telecom.CallAudioState.ROUTE_EARPIECE
                                        CallManager.setAudioRoute(route)
                                    } else {
                                        // Fallback
                                        val callService = CallManager.inCallService
                                        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager; am.isSpeakerphoneOn = isSpeakerOn
                                    }
             })
        } else {
            Toast.makeText(context, "Permission required to record audio.", Toast.LENGTH_SHORT).show()
        }
    }

    // Call status string
    val statusText = when (state) {
        android.telecom.Call.STATE_DIALING -> "Calling..."
        android.telecom.Call.STATE_RINGING -> "Incoming call"
        android.telecom.Call.STATE_ACTIVE -> "00:00" // You can implement a timer here
        android.telecom.Call.STATE_HOLDING -> "On hold"
        android.telecom.Call.STATE_DISCONNECTED -> "Call ended"
        else -> ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Background color of the call screen
            .padding(top = 48.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        // Top section: Contact Info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text(
                text = callerName,
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = phoneNumber,
                fontSize = 16.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SIM 1 ",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = statusText,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Placeholder for Contact Image / Background art
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F0FE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Contact Image",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF4285F4)
                )
            }
        }

        // Action grid (Record, Hold, Mute, etc)
        // We only show these if the call is active or connected
        if (state != android.telecom.Call.STATE_RINGING && state != android.telecom.Call.STATE_DISCONNECTED) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CallActionButton(
                        icon = Icons.Filled.Star,
                        label = "Record",
                        isActive = isRecording,
                        activeColor = Color(0xFFEA4335), // Red when recording
                        onClick = {
                            if (isRecording) {
                                stopRecording(mediaRecorder, { isRecording = false })
                            } else {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    startRecording(context, callerName, phoneNumber, { mediaRecorder = it }, { isRecording = true }, {
                                    isSpeakerOn = true
                                    val callService = CallManager.callState.value.call
                                    if (callService != null) {
                                        val route = if (isSpeakerOn) android.telecom.CallAudioState.ROUTE_SPEAKER else android.telecom.CallAudioState.ROUTE_EARPIECE
                                        CallManager.setAudioRoute(route)
                                    } else {
                                        // Fallback
                                        val callService = CallManager.inCallService
                                        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager; am.isSpeakerphoneOn = isSpeakerOn
                                    }
                                     })
                                } else {
                                    recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                    )
                    CallActionButton(
                        icon = Icons.Filled.Settings, // Replace with proper Hold icon if available
                        label = "Hold",
                        isActive = isOnHold,
                        onClick = {
                            isOnHold = !isOnHold
                            if (isOnHold) call?.hold() else call?.unhold()
                        }
                    )
                    CallActionButton(
                        icon = Icons.Filled.Add, // Replace with Video call icon
                        label = "Video call",
                        isActive = false,
                        onClick = { /* TODO */ }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CallActionButton(
                        icon = Icons.Filled.MicOff,
                        label = "Mute",
                        isActive = isMuted,
                        onClick = {
                            isMuted = !isMuted
                            val callService = CallManager.inCallService
                            if (callService != null) callService.setMuted(isMuted) else {
                                val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                                am.isMicrophoneMute = isMuted
                            }
                        }
                    )
                    CallActionButton(
                        icon = Icons.Filled.Phone, // Keypad icon
                        label = "Keypad",
                        isActive = false,
                        onClick = { /* TODO */ }
                    )
                    CallActionButton(
                        icon = Icons.Filled.VolumeUp,
                        label = "Speaker",
                        isActive = isSpeakerOn,
                        onClick = {
                            isSpeakerOn = !isSpeakerOn
                            val callService = CallManager.callState.value.call
                            if (callService != null) {
                                val route = if (isSpeakerOn) android.telecom.CallAudioState.ROUTE_SPEAKER else android.telecom.CallAudioState.ROUTE_EARPIECE
                                CallManager.setAudioRoute(route)
                            } else {
                                // Fallback
                                val callService = CallManager.inCallService
                                val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager; am.isSpeakerphoneOn = isSpeakerOn
                            }
                        }
                    )
                }
            }
        }

        // Bottom actions (Answer/Decline/End)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (state == android.telecom.Call.STATE_RINGING) {
                // Incoming call: Answer and Decline buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Decline Button
                    FloatingActionButton(
                        onClick = { call?.disconnect() },
                        containerColor = Color(0xFFEA4335),
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Decline call",
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Answer Button
                    FloatingActionButton(
                        onClick = { call?.answer(call.details.videoState) },
                        containerColor = Color(0xFF34A853),
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = "Answer call",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            } else {
                // End call button
                FloatingActionButton(
                    onClick = {
                        if (isRecording) {
                            stopRecording(mediaRecorder, { isRecording = false })
                        }
                        call?.disconnect()
                        navController?.popBackStack()
                    },
                    containerColor = Color(0xFFEA4335),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "End call",
                        modifier = Modifier.size(36.dp)
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
    isActive: Boolean,
    activeColor: Color = Color(0xFF4285F4),
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (isActive) Color.LightGray else Color.Transparent)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else Color.DarkGray,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )
    }
}

private fun startRecording(
    context: Context,
    callerName: String,
    phoneNumber: String,
    setRecorder: (MediaRecorder) -> Unit,
    setRecording: () -> Unit,
    setSpeakerOn: () -> Unit
) {
    // Try sources in order of preference for two-way audio (earpiece, speaker, etc)
    // The only bulletproof way to capture two-way audio is to use MIC and force the speakerphone on.

    val timestamp = System.currentTimeMillis()
    val sanitizedName = callerName.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
    val fileName = "CallRecord_${sanitizedName}_${phoneNumber}_${timestamp}.3gp"

    setSpeakerOn()

    val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
    } else {
        @Suppress("DEPRECATION")
        MediaRecorder()
    }

    try {
        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            // Use MediaStore API to save the recording
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "audio/3gpp")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/CallRecordings")
                }
            }

            val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                val fileDescriptor = resolver.openFileDescriptor(uri, "w")?.fileDescriptor
                if (fileDescriptor != null) {
                    setOutputFile(fileDescriptor)
                    prepare()
                    start()
                    setRecorder(this)
                    setRecording()
                    Toast.makeText(context, "Recording started (Mic + Speaker). Save to Music/CallRecordings", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to get file descriptor", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Failed to create media store entry", Toast.LENGTH_SHORT).show()
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Recording failed: ${e.message}", Toast.LENGTH_SHORT).show()
    } catch (e: IllegalStateException) {
        e.printStackTrace()
        Toast.makeText(context, "Recording failed (State): ${e.message}", Toast.LENGTH_SHORT).show()
    } catch (e: RuntimeException) {
        e.printStackTrace()
        Toast.makeText(context, "Recording failed (Runtime). AudioSource may be locked.", Toast.LENGTH_SHORT).show()
    }
}

private fun stopRecording(recorder: MediaRecorder?, setRecording: () -> Unit) {
    try {
        recorder?.apply {
            stop()
            reset()
            release()
        }
        setRecording()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
