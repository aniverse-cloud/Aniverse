import re

filepath = 'AnPhone001/app/src/main/java/com/example/dialer/CallScreen.kt'

with open(filepath, 'r') as f:
    content = f.read()

# Fix VolumeUp import for Material Icons (it's not AutoMirrored in older compose versions, let's use a standard icon like PhoneInTalk or just Revert to Call but change color)
# Better yet, let's just use Icons.Default.PhoneVolume or similar if VolumeUp is part of extended.

search_block = """import androidx.compose.material.icons.automirrored.filled.VolumeUp"""
replace_block = """// removed VolumeUp"""

if search_block in content:
    content = content.replace(search_block, replace_block)

    # Also need to update the icon usage back to something standard like Icons.Filled.Call
    content = content.replace('Icons.AutoMirrored.Filled.VolumeUp', 'Icons.Filled.Call')

    with open(filepath, 'w') as f:
        f.write(content)
    print("Fixed icon import issue.")
else:
    print("Could not find the target block to replace.")
