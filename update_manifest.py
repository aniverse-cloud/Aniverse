import re

filepath = 'AnPhone001/app/src/main/AndroidManifest.xml'

with open(filepath, 'r') as f:
    content = f.read()

# Add CAPTURE_AUDIO_OUTPUT for system-level call recording (might be needed for VOICE_CALL on some devices)
if 'android.permission.CAPTURE_AUDIO_OUTPUT' not in content:
    content = content.replace(
        '<uses-permission android:name="android.permission.RECORD_AUDIO" />',
        '<uses-permission android:name="android.permission.RECORD_AUDIO" />\n    <uses-permission android:name="android.permission.CAPTURE_AUDIO_OUTPUT" />'
    )
    with open(filepath, 'w') as f:
        f.write(content)
    print("Added CAPTURE_AUDIO_OUTPUT to AndroidManifest.xml")
else:
    print("Permission already present.")
