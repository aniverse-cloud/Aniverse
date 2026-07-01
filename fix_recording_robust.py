import re

filepath = 'AnPhone001/app/src/main/java/com/example/dialer/CallScreen.kt'

with open(filepath, 'r') as f:
    content = f.read()

# Completely rewrite the `startRecording` function to cleanly handle failures
# using MediaRecorder.AudioSource.MIC as it's the safest that can capture both if speakerphone is on
# but we can try VOICE_COMMUNICATION first.
import sys

# Find the function definition
start = content.find("private fun startRecording(")
if start == -1:
    print("Could not find startRecording")
    sys.exit(1)

# Find the end of the function (rough heuristic)
end = content.find("fun CallActionButton", start)
if end == -1:
    print("Could not find end of startRecording")
    sys.exit(1)

new_func = """private fun startRecording(context: Context, callerName: String, phoneNumber: String, setRecorder: (MediaRecorder) -> Unit, setRecordingState: () -> Unit) {
    try {
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        // For third-party apps, MIC or VOICE_COMMUNICATION are the only reliable sources.
        // VOICE_CALL is heavily restricted by OEMs and usually throws RuntimeException on start() or prepare().
        // To get both sides, users typically need to use speakerphone with MIC/VOICE_COMMUNICATION.
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
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
"""

content = content[:start] + new_func + content[end + len("@Composable\n"):]

with open(filepath, 'w') as f:
    f.write(content)
print("Rewrote startRecording to use MIC.")
