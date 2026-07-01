import re

filepath = 'AnPhone001/app/src/main/java/com/example/dialer/CallScreen.kt'

with open(filepath, 'r') as f:
    content = f.read()

# Fix VolumeUp import for Material Icons Extended (or just use standard Material Icons)
search_block = """import androidx.compose.material.icons.filled.VolumeUp"""
replace_block = """import androidx.compose.material.icons.automirrored.filled.VolumeUp"""

if search_block in content:
    content = content.replace(search_block, replace_block)

    # Also need to update the icon usage
    content = content.replace('Icons.Filled.VolumeUp', 'Icons.AutoMirrored.Filled.VolumeUp')

    with open(filepath, 'w') as f:
        f.write(content)
    print("Fixed VolumeUp import.")
else:
    print("Could not find the target block to replace.")
