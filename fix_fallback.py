import re

filepath = 'AnPhone001/app/src/main/java/com/example/dialer/CallScreen.kt'

with open(filepath, 'r') as f:
    content = f.read()

import sys

start = content.find("private fun startRecording(")
if start == -1:
    print("Could not find startRecording")
    sys.exit(1)

end = content.find("fun CallActionButton", start)
if end == -1:
    print("Could not find end of startRecording")
    sys.exit(1)

new_func = """private fun startRecording(context: Context, callerName: String, phoneNumber: String, setRecorder: (MediaRecorder) -> Unit, setRecordingState: () -> Unit) {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

    // Format the file name
    val safeName = callerName.replace(Regex("[^a-zA-Z0-9]"), "").takeIf { it.isNotBlank() }
    val safeNumber = phoneNumber.replace(Regex("[^0-9+]"), "").takeIf { it.isNotBlank() } ?: "Unknown"

    val fileName = if (safeName != null && safeName != "UnknownContact" && safeName != "Unknown") {
        "Call_${safeName}_${safeNumber}_$timeStamp.m4a"
    } else {
        "Call_${safeNumber}_$timeStamp.m4a"
    }

    // Determine output destination
    val resolver = context.contentResolver
    var audioUri: android.net.Uri? = null
    var fallbackFile: File? = null

    fun getOutputFileDescriptor(): java.io.FileDescriptor? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp4")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/CallRecordings")
            }
            audioUri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
            return audioUri?.let { resolver.openFileDescriptor(it, "w")?.fileDescriptor }
        } else {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            if (!dir.exists()) { dir.mkdirs() }
            fallbackFile = File(dir, fileName)
            return java.io.FileInputStream(fallbackFile).fd // Actually we need path, but we handle it below
        }
    }

    fun getOutputFilePath(): String? {
       return fallbackFile?.absolutePath
    }

    fun cleanUpFailedFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            audioUri?.let { resolver.delete(it, null, null) }
        } else {
            fallbackFile?.delete()
        }
    }

    // Try sources in order of preference for two-way audio (earpiece, speaker, etc)
    val audioSources = listOf(
        MediaRecorder.AudioSource.VOICE_CALL,          // Best for both sides, highly restricted
        MediaRecorder.AudioSource.VOICE_COMMUNICATION, // Echo cancellation, good for speaker/headset
        MediaRecorder.AudioSource.VOICE_RECOGNITION,   // Often bypasses AGC/filters
        MediaRecorder.AudioSource.MIC                  // Absolute fallback, relies on ambient sound if not speaker
    )

    var successfulRecorder: MediaRecorder? = null
    var usedSource = -1

    for (source in audioSources) {
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        try {
            recorder.setAudioSource(source)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val fd = getOutputFileDescriptor()
                if (fd != null) recorder.setOutputFile(fd) else throw Exception("FD is null")
            } else {
                getOutputFileDescriptor() // sets fallbackFile
                recorder.setOutputFile(getOutputFilePath())
            }

            // The prepare() and start() methods are where it throws if the source is blocked by the OS
            recorder.prepare()
            recorder.start()

            successfulRecorder = recorder
            usedSource = source
            break // Success! Exit the loop.
        } catch (e: Exception) {
            // Failed, clean up this attempt
            recorder.reset()
            recorder.release()
            cleanUpFailedFile()
        }
    }

    if (successfulRecorder != null) {
        setRecorder(successfulRecorder)
        setRecordingState()

        val sourceName = when(usedSource) {
            MediaRecorder.AudioSource.VOICE_CALL -> "VOICE_CALL"
            MediaRecorder.AudioSource.VOICE_COMMUNICATION -> "VOICE_COMMUNICATION"
            MediaRecorder.AudioSource.VOICE_RECOGNITION -> "VOICE_RECOGNITION"
            MediaRecorder.AudioSource.MIC -> "MIC"
            else -> "UNKNOWN"
        }

        val extraMsg = if (usedSource == MediaRecorder.AudioSource.MIC) " (Enable speaker for 2-way)" else ""
        Toast.makeText(context, "Recording ($sourceName) to Music/$fileName$extraMsg", Toast.LENGTH_LONG).show()
    } else {
        Toast.makeText(context, "All recording sources failed or are blocked by OS.", Toast.LENGTH_LONG).show()
    }
}

@Composable
"""

content = content[:start] + new_func + content[end + len("@Composable\n"):]

with open(filepath, 'w') as f:
    f.write(content)
print("Rewrote startRecording with robust fallback loop.")
