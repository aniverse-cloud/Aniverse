package com.example.dialer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.telecom.Call
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.content.ContentValues
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
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
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CallScreen(navController: NavController) {
    val context = LocalContext.current
    val callStateWrapper by CallManager.callState.collectAsState()
    val call = callStateWrapper.call
    val state = callStateWrapper.state
    val details = callStateWrapper.details

    var isMuted by remember { mutableStateOf(false) }
    var isOnHold by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var mediaRecorder: MediaRecorder? by remember { mutableStateOf(null) }

    val phoneNumber = details?.handle?.schemeSpecificPart ?: "Unknown"
    val callerName = details?.callerDisplayName ?: "Unknown Contact"

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startRecording(context, callerName, phoneNumber, { mediaRecorder = it }, { isRecording = true })
        } else {
            Toast.makeText(context, "Permission required to record calls.", Toast.LENGTH_SHORT).show()
        }
    }

    // Dual SIM detection
    var simInfo by remember { mutableStateOf("Unknown SIM") }
    LaunchedEffect(details) {
        details?.accountHandle?.let { handle ->
            // Try to extract SIM info from PhoneAccountHandle
            val id = handle.id
            if (id.contains("0") || id.contains("1", ignoreCase = true)) {
                simInfo = "SIM 1"
            } else if (id.contains("1") || id.contains("2", ignoreCase = true)) {
                simInfo = "SIM 2"
            } else {
                 simInfo = "SIM ${handle.id.take(4)}" // Fallback
            }
        }
    }

    val callStateLabel = when (state) {
        Call.STATE_RINGING -> "Ringing"
        Call.STATE_DIALING -> "Dialing"
        Call.STATE_ACTIVE -> "Active"
        Call.STATE_HOLDING -> "On Hold"
        Call.STATE_DISCONNECTED -> "Disconnected"
        else -> "Connecting..."
    }

    LaunchedEffect(state) {
        isOnHold = state == Call.STATE_HOLDING

        if (state == Call.STATE_DISCONNECTED && isRecording) {
            try {
                mediaRecorder?.stop()
                mediaRecorder?.release()
                mediaRecorder = null
                isRecording = false
                Toast.makeText(context, "Call ended, recording saved.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF2F3F5)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding(), // Ensures content is drawn edge-to-edge but text doesn't overlap status bar
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = callerName,
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
                    text = "$simInfo - $callStateLabel",
                    fontSize = 16.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CallActionButton(
                        icon = Icons.Filled.MoreVert,
                        label = "Notes",
                        onClick = { /* TODO */ }
                    )
                    CallActionButton(
                        icon = Icons.Filled.Add,
                        label = "Add call",
                        onClick = {
                            call?.hold()
                            Toast.makeText(context, "Call held to add another.", Toast.LENGTH_SHORT).show()
                        }
                    )
                    CallActionButton(
                        icon = Icons.Filled.Settings,
                        label = "Mute",
                        isActive = isMuted,
                        onClick = {
                            isMuted = !isMuted
                            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                            am.isMicrophoneMute = isMuted
                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CallActionButton(
                        icon = Icons.Filled.Star,
                        label = if (isRecording) "Recording..." else "Record",
                        isActive = isRecording,
                        activeColor = Color.Red, // Highlight red when recording
                        onClick = {
                            if (!isRecording) {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    startRecording(context, callerName, phoneNumber, { mediaRecorder = it }, { isRecording = true })
                                } else {
                                    recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            } else {
                                try {
                                    mediaRecorder?.stop()
                                    mediaRecorder?.release()
                                    mediaRecorder = null
                                    isRecording = false
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    )
                    CallActionButton(
                        icon = Icons.Filled.Person,
                        label = "Contacts",
                        onClick = { /* TODO */ }
                    )
                    CallActionButton(
                        icon = Icons.Filled.Close,
                        label = "Hold",
                        isActive = isOnHold,
                        onClick = {
                            if (isOnHold) {
                                call?.unhold()
                            } else {
                                call?.hold()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* TODO */ }) {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = "Keypad",
                        modifier = Modifier.size(32.dp),
                        tint = Color.DarkGray
                    )
                }

                FloatingActionButton(
                    onClick = {
                        call?.let {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && it.state == Call.STATE_RINGING) {
                                it.reject(Call.REJECT_REASON_DECLINED)
                            } else if (it.state == Call.STATE_RINGING) {
                                it.reject(false, null)
                            } else {
                                it.disconnect()
                            }
                        } ?: run {
                            navController.popBackStack()
                        }

                        if(isRecording) {
                            try {
                                mediaRecorder?.stop()
                                mediaRecorder?.release()
                                mediaRecorder = null
                                isRecording = false
                            } catch (e: Exception) {}
                        }
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

                IconButton(onClick = {
                    isSpeakerOn = !isSpeakerOn
                    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    am.isSpeakerphoneOn = isSpeakerOn
                }) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "Speaker",
                        modifier = Modifier.size(32.dp),
                        tint = if (isSpeakerOn) MaterialTheme.colorScheme.primary else Color.DarkGray
                    )
                }
            }
        }
    }
}

private fun startRecording(context: Context, callerName: String, phoneNumber: String, setRecorder: (MediaRecorder) -> Unit, setRecordingState: () -> Unit) {
    try {
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        try {
            // Attempt to record both uplink (microphone) and downlink (speaker)
            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL)
        } catch (e: Exception) {
            // Fallback to VOICE_COMMUNICATION or MIC if system prevents VOICE_CALL
            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
        }
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

        // Format the file name based on whether we have a contact name or just a number
        val safeName = callerName.replace(Regex("[^a-zA-Z0-9]"), "").takeIf { it.isNotBlank() }
        val safeNumber = phoneNumber.replace(Regex("[^0-9+]"), "").takeIf { it.isNotBlank() } ?: "Unknown"

        val fileName = if (safeName != null && safeName != "UnknownContact" && safeName != "Unknown") {
            "Call_${safeName}_${safeNumber}_$timeStamp.m4a"
        } else {
            "Call_${safeNumber}_$timeStamp.m4a"
        }

        // Use MediaStore for Android Q and above, fallback to File for older versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp4")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/CallRecordings")
            }
            val audioUri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (audioUri != null) {
                val pfd = resolver.openFileDescriptor(audioUri, "w")
                recorder.setOutputFile(pfd?.fileDescriptor)
            } else {
                throw Exception("Failed to create MediaStore entry")
            }
        } else {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            if (!dir.exists()) { dir.mkdirs() }
            val file = File(dir, fileName)
            recorder.setOutputFile(file.absolutePath)
        }

        recorder.prepare()
        recorder.start()

        setRecorder(recorder)
        setRecordingState()
        Toast.makeText(context, "Recording started. Saving to Music/$fileName", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to start recording: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun CallActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean = false,
    activeColor: Color = Color.Black,
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
                tint = if (isActive) activeColor else Color.DarkGray,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = if (isActive && activeColor == Color.Red) Color.Red else Color.DarkGray
        )
    }
}
