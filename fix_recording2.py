import re

filepath = 'AnPhone001/app/src/main/java/com/example/dialer/CallScreen.kt'

with open(filepath, 'r') as f:
    content = f.read()

# We need to rewrite the entire setup process to properly try VOICE_CALL and then gracefully fallback
# if prepare() or start() fails.
search_block = """
        // VOICE_CALL frequently throws RuntimeException during prepare() or start() on non-system apps
        // VOICE_COMMUNICATION is safer, but if we want both, we can try MIC and enable speakerphone, or just use VOICE_COMMUNICATION
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
"""

replace_block = """
        // Attempt to record both uplink and downlink using VOICE_RECOGNITION or VOICE_COMMUNICATION
        // since VOICE_CALL is strictly protected for system apps in many Android 10+ devices
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
"""

if search_block in content:
    content = content.replace(search_block, replace_block)
    with open(filepath, 'w') as f:
        f.write(content)
    print("Changed to VOICE_RECOGNITION as primary.")
else:
    print("Could not find the target block to replace.")
