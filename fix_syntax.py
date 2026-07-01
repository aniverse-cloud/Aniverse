import re

filepath = 'AnPhone001/app/src/main/java/com/example/dialer/CallScreen.kt'

with open(filepath, 'r') as f:
    content = f.read()

# Fix the syntax error around @Composable onButton -> @Composable fun CallActionButton
search_block = """@Composable
onButton(
    icon: ImageVector,
    label: String,"""

replace_block = """@Composable
fun CallActionButton(
    icon: ImageVector,
    label: String,"""

if search_block in content:
    content = content.replace(search_block, replace_block)
    with open(filepath, 'w') as f:
        f.write(content)
    print("Fixed syntax error.")
else:
    print("Could not find the target block to replace.")
