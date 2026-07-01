import re

filepath = 'AnPhone001/app/src/main/java/com/example/dialer/CallScreen.kt'

with open(filepath, 'r') as f:
    content = f.read()

# Fix the syntax error again
search_block = """@Composable
onButton(
    icon: ImageVector,"""

replace_block = """@Composable
fun CallActionButton(
    icon: ImageVector,"""

if search_block in content:
    content = content.replace(search_block, replace_block)
    with open(filepath, 'w') as f:
        f.write(content)
    print("Fixed syntax error.")
else:
    print("Could not find the target block to replace.")
