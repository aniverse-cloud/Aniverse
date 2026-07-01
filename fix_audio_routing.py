import re

filepath = 'AnPhone001/app/src/main/java/com/example/dialer/CallScreen.kt'

with open(filepath, 'r') as f:
    content = f.read()

# Fix the speaker routing logic.
# 1. In CallService, TelecomManager handles the audio routing for calls. AudioManager is SpeakerphoneOn often fails when TelecomManager is in control of the call state.
# We should use InCallService.setAudioRoute(CallAudioState.ROUTE_SPEAKER)

search_block = """val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    am.isSpeakerphoneOn = isSpeakerOn"""
replace_block = """val callService = CallManager.callState.value.call
                    if (callService != null) {
                        val route = if (isSpeakerOn) android.telecom.CallAudioState.ROUTE_SPEAKER else android.telecom.CallAudioState.ROUTE_EARPIECE
                        CallManager.setAudioRoute(route)
                    } else {
                        // Fallback
                        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                        am.isSpeakerphoneOn = isSpeakerOn
                    }"""

content = content.replace(search_block, replace_block)

search_block_auto = """isSpeakerOn = true
                                    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                                    am.isSpeakerphoneOn = true"""

replace_block_auto = """isSpeakerOn = true
                                    val callService = CallManager.callState.value.call
                                    if (callService != null) {
                                        CallManager.setAudioRoute(android.telecom.CallAudioState.ROUTE_SPEAKER)
                                    } else {
                                        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                                        am.isSpeakerphoneOn = true
                                    }"""

content = content.replace(search_block_auto, replace_block_auto)

with open(filepath, 'w') as f:
    f.write(content)
print("Updated speaker routing to use TelecomManager.")
