import re

filepath = 'AnPhone001/app/src/main/java/com/example/dialer/CallService.kt'

with open(filepath, 'r') as f:
    content = f.read()

# Make sure CallService sets itself on CallManager
search_block = """    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        CallManager.updateCall(call)"""

replace_block = """    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        CallManager.inCallService = this
        CallManager.updateCall(call)"""

content = content.replace(search_block, replace_block)

with open(filepath, 'w') as f:
    f.write(content)
print("Bound InCallService to CallManager.")
