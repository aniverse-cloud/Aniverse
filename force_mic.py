import re

filepath = 'AnPhone001/app/src/main/java/com/example/dialer/CallScreen.kt'

with open(filepath, 'r') as f:
    content = f.read()

# Modify startRecording signature to accept an action to enable speaker
search_block = """private fun startRecording(context: Context, callerName: String, phoneNumber: String, setRecorder: (MediaRecorder) -> Unit, setRecordingState: () -> Unit) {"""
replace_block = """private fun startRecording(context: Context, callerName: String, phoneNumber: String, setRecorder: (MediaRecorder) -> Unit, setRecordingState: () -> Unit, enableSpeaker: () -> Unit) {"""

content = content.replace(search_block, replace_block)

# Modify the call to startRecording
search_block_call1 = """startRecording(context, callerName, phoneNumber, { mediaRecorder = it }, { isRecording = true })"""
replace_block_call1 = """startRecording(context, callerName, phoneNumber, { mediaRecorder = it }, { isRecording = true }, {
                                    isSpeakerOn = true
                                    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                                    am.isSpeakerphoneOn = true
                                })"""

content = content.replace(search_block_call1, replace_block_call1)

# Ensure only MIC is used since it's the only one that never gets zeroed out by telephony stack.
search_block_audio = """    // For Android 10+, VOICE_RECOGNITION and MIC are often the only sources that don't get silenced.
    // Additionally, the AAC hardware encoder is often locked by the modem during a call, resulting in a silent file.
    // Using THREE_GPP and AMR_NB is a common workaround to get audio when AAC produces silence.
    val audioSources = listOf(
        MediaRecorder.AudioSource.VOICE_RECOGNITION,
        MediaRecorder.AudioSource.MIC,
        MediaRecorder.AudioSource.VOICE_COMMUNICATION
    )"""

replace_block_audio = """    // For non-system dialers, telephony framework actively zeros out buffers for VOICE_CALL, VOICE_COMMUNICATION,
    // and sometimes VOICE_RECOGNITION during a call, rather than throwing an exception.
    // The only bulletproof way to capture two-way audio is to use MIC and force the speakerphone on.
    val audioSources = listOf(
        MediaRecorder.AudioSource.MIC
    )"""

content = content.replace(search_block_audio, replace_block_audio)

# Enable speaker when MIC is used
search_block_success = """    if (successfulRecorder != null) {
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
    } else {"""

replace_block_success = """    if (successfulRecorder != null) {
        setRecorder(successfulRecorder)
        setRecordingState()

        if (usedSource == MediaRecorder.AudioSource.MIC) {
            enableSpeaker()
            Toast.makeText(context, "Recording via MIC. Speaker auto-enabled.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Recording started to Music/$fileName", Toast.LENGTH_LONG).show()
        }
    } else {"""

content = content.replace(search_block_success, replace_block_success)

with open(filepath, 'w') as f:
    f.write(content)
print("Updated recording logic to force MIC and auto-enable speaker.")
