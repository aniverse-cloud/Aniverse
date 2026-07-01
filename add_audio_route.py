import re

filepath = 'AnPhone001/app/src/main/java/com/example/dialer/CallManager.kt'

with open(filepath, 'r') as f:
    content = f.read()

# Add a function to handle audio routing via the InCallService
import_block = """import android.telecom.Call
import kotlinx.coroutines.flow.MutableStateFlow"""
replace_import = """import android.telecom.Call
import android.telecom.InCallService
import kotlinx.coroutines.flow.MutableStateFlow"""

if import_block in content:
    content = content.replace(import_block, replace_import)

# Add reference to InCallService instance
manager_block = """object CallManager {"""
replace_manager = """object CallManager {
    var inCallService: InCallService? = null

    fun setAudioRoute(route: Int) {
        inCallService?.setAudioRoute(route)
    }
"""

content = content.replace(manager_block, replace_manager)

with open(filepath, 'w') as f:
    f.write(content)
print("Added setAudioRoute to CallManager.")
