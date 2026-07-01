import re

filepath = 'AnPhone001/app/src/main/java/com/example/dialer/CallScreen.kt'

with open(filepath, 'r') as f:
    content = f.read()

search_block = """        setRecorder(recorder)
        setRecordingState()
        Toast.makeText(context, "Recording started. Saving to Music/$fileName", Toast.LENGTH_LONG).show()"""

replace_block = """        setRecorder(recorder)
        setRecordingState()
        // Inform user about speakerphone requirement for two-way audio on third-party apps
        Toast.makeText(context, "Recording started. Enable speaker for two-way audio.", Toast.LENGTH_LONG).show()"""

if search_block in content:
    content = content.replace(search_block, replace_block)
    with open(filepath, 'w') as f:
        f.write(content)
    print("Added UX toast for speakerphone requirement.")
else:
    print("Could not find the target block to replace.")
