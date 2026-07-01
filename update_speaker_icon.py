import re

filepath = 'AnPhone001/app/src/main/java/com/example/dialer/CallScreen.kt'

with open(filepath, 'r') as f:
    content = f.read()

# Replace the speaker icon
search_block = """                IconButton(onClick = {
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
                }"""

replace_block = """                IconButton(onClick = {
                    isSpeakerOn = !isSpeakerOn
                    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    am.isSpeakerphoneOn = isSpeakerOn
                }) {
                    Icon(
                        imageVector = Icons.Filled.VolumeUp,
                        contentDescription = "Speaker",
                        modifier = Modifier.size(32.dp),
                        tint = if (isSpeakerOn) Color(0xFF4285F4) else Color.DarkGray
                    )
                }"""

if search_block in content:
    content = content.replace(search_block, replace_block)
    with open(filepath, 'w') as f:
        f.write(content)
    print("Updated speaker icon and color.")
else:
    print("Could not find the target block to replace.")
