import re

filepath = 'AnPhone001/app/src/main/java/com/example/dialer/CallScreen.kt'

with open(filepath, 'r') as f:
    content = f.read()

# Replace the specific MediaRecorder configuration block
# Many third-party apps fail on prepare() if they try to use VOICE_CALL.
# We can't just catch setAudioSource because it doesn't throw until prepare().
# Let's use VOICE_COMMUNICATION as the primary for a safer default, or we can catch prepare().
search_block = """
        try {
            // Attempt to record both uplink (microphone) and downlink (speaker)
            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL)
        } catch (e: Exception) {
            // Fallback to VOICE_COMMUNICATION or MIC if system prevents VOICE_CALL
            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
        }
"""

replace_block = """
        // VOICE_CALL frequently throws RuntimeException during prepare() or start() on non-system apps
        // VOICE_COMMUNICATION is safer, but if we want both, we can try MIC and enable speakerphone, or just use VOICE_COMMUNICATION
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
"""

if search_block in content:
    content = content.replace(search_block, replace_block)
    with open(filepath, 'w') as f:
        f.write(content)
    print("Reverted to VOICE_COMMUNICATION to prevent failure.")
else:
    print("Could not find the target block to replace.")
