import re

filepath = 'AnPhone001/app/src/main/java/com/example/dialer/CallScreen.kt'

with open(filepath, 'r') as f:
    content = f.read()

# Replace the specific MediaRecorder configuration block
# Add a try/catch block to fallback to MIC if VOICE_CALL is restricted
search_block = """
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
"""

replace_block = """
        try {
            // Attempt to record both uplink (microphone) and downlink (speaker)
            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL)
        } catch (e: Exception) {
            // Fallback to VOICE_COMMUNICATION or MIC if system prevents VOICE_CALL
            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
        }
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
"""

if search_block in content:
    content = content.replace(search_block, replace_block)
    with open(filepath, 'w') as f:
        f.write(content)
    print("Successfully updated audio source with fallback.")
else:
    print("Could not find the target block to replace.")
