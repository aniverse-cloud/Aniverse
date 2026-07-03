package com.example.dialer

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.text.SimpleDateFormat
import java.util.*

data class ContactOrCall(
    val name: String,
    val number: String,
    val date: String? = null,
    val type: Int? = null,
    val isRecentCall: Boolean = true
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DialerScreen(navController: NavController? = null) {
    var phoneNumber by remember { mutableStateOf("") }
    var isKeypadVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS
        )
    )

    var recentCalls by remember { mutableStateOf<List<ContactOrCall>>(emptyList()) }
    var allContacts by remember { mutableStateOf<List<ContactOrCall>>(emptyList()) }

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            val contentResolver: ContentResolver = context.contentResolver

            // Fetch Recent Calls
            val cursorCalls = contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.TYPE, CallLog.Calls.CACHED_NAME),
                null, null, CallLog.Calls.DATE + " DESC"
            )
            val fetchedCalls = mutableListOf<ContactOrCall>()
            cursorCalls?.use {
                val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
                val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
                val nameIndex = it.getColumnIndex(CallLog.Calls.CACHED_NAME)

                while (it.moveToNext() && fetchedCalls.size < 100) {
                    val number = it.getString(numberIndex) ?: ""
                    val dateMillis = it.getLong(dateIndex)
                    val type = it.getInt(typeIndex)
                    val name = it.getString(nameIndex) ?: number

                    val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                    val dateString = formatter.format(Date(dateMillis))

                    fetchedCalls.add(ContactOrCall(name, number, dateString, type, true))
                }
            }
            recentCalls = fetchedCalls

            // Fetch Contacts
            val cursorContacts = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
                null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
            val fetchedContacts = mutableListOf<ContactOrCall>()
            cursorContacts?.use {
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (it.moveToNext()) {
                    val name = it.getString(nameIndex) ?: "Unknown"
                    var number = it.getString(numberIndex) ?: ""
                    // Basic normalization
                    number = number.replace(" ", "").replace("-", "")
                    fetchedContacts.add(ContactOrCall(name, number, isRecentCall = false))
                }
            }
            allContacts = fetchedContacts.distinctBy { it.name + it.number }
        } else {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    val displayList = remember(phoneNumber, recentCalls, allContacts) {
        if (phoneNumber.isEmpty()) {
            recentCalls
        } else {
            val filteredContacts = allContacts.filter { it.number.contains(phoneNumber) }
            val filteredRecents = recentCalls.filter { it.number.contains(phoneNumber) }

            // Combine and deduplicate by number, preferring contacts with names
            val combined = (filteredContacts + filteredRecents).distinctBy { it.number }
            combined
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header for Input if keypad is visible
            if (isKeypadVisible) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = phoneNumber,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (phoneNumber.isNotEmpty()) {
                        IconButton(onClick = { phoneNumber = phoneNumber.dropLast(1) }) {
                            Icon(Icons.Filled.Backspace, contentDescription = "Delete last digit")
                        }
                    }
                }
            }

            // List of calls/contacts
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(displayList) { item ->
                    ListItem(
                        modifier = Modifier.clickable {
                            if (permissionsState.allPermissionsGranted) {
                                val intent = Intent(Intent.ACTION_CALL).apply {
                                    data = Uri.parse("tel:${item.number}")
                                }
                                context.startActivity(intent)
                            }
                        },
                        headlineContent = {
                            val headlineText = if (item.name.isEmpty()) item.number else item.name
                            Text(headlineText, fontWeight = FontWeight.Bold)
                        },
                        supportingContent = {
                            val subText = if (item.isRecentCall && item.date != null && item.type != null) {
                                "${item.number} • ${item.date} • ${getCallTypeString(item.type)}"
                            } else {
                                item.number
                            }
                            Text(subText)
                        },
                        trailingContent = {
                            Icon(Icons.Filled.Call, contentDescription = "Call ${item.name}")
                        }
                    )
                    Divider()
                }
            }

            // Keypad at the bottom
            if (isKeypadVisible) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val keys = listOf(
                        "1" to "", "2" to "ABC", "3" to "DEF",
                        "4" to "GHI", "5" to "JKL", "6" to "MNO",
                        "7" to "PQRS", "8" to "TUV", "9" to "WXYZ",
                        "*" to "", "0" to "+", "#" to ""
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        items(keys) { (number, letters) ->
                            DialerKey(
                                number = number,
                                letters = letters,
                                onClick = { phoneNumber += number }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FloatingActionButton(
                            onClick = {
                                if (phoneNumber.isNotEmpty()) {
                                    if (permissionsState.allPermissionsGranted) {
                                        val intent = Intent(Intent.ACTION_CALL).apply {
                                            data = Uri.parse("tel:$phoneNumber")
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(Icons.Filled.Call, contentDescription = "Call", modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }
        }

        // FAB to show keypad when hidden
        if (!isKeypadVisible) {
            FloatingActionButton(
                onClick = { isKeypadVisible = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Filled.Dialpad, contentDescription = "Open Keypad")
            }
        } else if (phoneNumber.isEmpty()) {
            // Button to hide keypad when it's empty
            FloatingActionButton(
                onClick = { isKeypadVisible = false },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(Icons.Filled.Dialpad, contentDescription = "Hide Keypad") // Could use a downward arrow instead
            }
        }
    }
}

// Needed since we're using getCallTypeString which was in RecentsScreen
fun getCallTypeString(type: Int): String {
    return when (type) {
        CallLog.Calls.INCOMING_TYPE -> "Incoming"
        CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
        CallLog.Calls.MISSED_TYPE -> "Missed"
        CallLog.Calls.REJECTED_TYPE -> "Rejected"
        else -> "Unknown"
    }
}

@Composable
fun DialerKey(number: String, letters: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = number, fontSize = 28.sp, fontWeight = FontWeight.Medium)
            if (letters.isNotEmpty()) {
                Text(text = letters, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
