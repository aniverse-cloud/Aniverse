import re

filepath = 'AnPhone001/app/src/main/java/com/example/dialer/CallScreen.kt'

with open(filepath, 'r') as f:
    content = f.read()

# Replace the fallback loop and encoder settings
# AAC encoder is often locked by the telephony stack during a call, which results in a silent 0-byte or silent-duration file.
# We will change to AMR_NB and 3GPP.

search_block = """    val audioSources = listOf(
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
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)"""

replace_block = """    // For Android 10+, VOICE_RECOGNITION and MIC are often the only sources that don't get silenced.
    // Additionally, the AAC hardware encoder is often locked by the modem during a call, resulting in a silent file.
    // Using THREE_GPP and AMR_NB is a common workaround to get audio when AAC produces silence.
    val audioSources = listOf(
        MediaRecorder.AudioSource.VOICE_RECOGNITION,
        MediaRecorder.AudioSource.MIC,
        MediaRecorder.AudioSource.VOICE_COMMUNICATION
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
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)"""

if search_block in content:
    content = content.replace(search_block, replace_block)

    # Also need to fix the file extension from .m4a to .3gp
    content = content.replace('audio/mp4', 'audio/3gpp')
    content = content.replace('.m4a', '.3gp')

    with open(filepath, 'w') as f:
        f.write(content)
    print("Fixed audio source and encoder settings.")
else:
    print("Could not find the target block to replace.")
