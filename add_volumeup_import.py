import re

filepath = 'AnPhone001/app/src/main/java/com/example/dialer/CallScreen.kt'

with open(filepath, 'r') as f:
    content = f.read()

if "import androidx.compose.material.icons.filled.VolumeUp" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Call",
                              "import androidx.compose.material.icons.filled.Call\nimport androidx.compose.material.icons.filled.VolumeUp")
    with open(filepath, 'w') as f:
        f.write(content)
    print("Added VolumeUp import.")
else:
    print("Import already exists.")
