package com.example.dialer

import android.Manifest
import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

data class Contact(val name: String, val number: String)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ContactsScreen(innerPadding: PaddingValues = PaddingValues(0.dp)) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.READ_CONTACTS)
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }

    LaunchedEffect(permissionState.status) {
        if (permissionState.status.isGranted) {
            val contentResolver: ContentResolver = context.contentResolver
            val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
                null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
            val fetchedContacts = mutableListOf<Contact>()
            cursor?.use {
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (it.moveToNext()) {
                    val name = it.getString(nameIndex) ?: "Unknown"
                    val number = it.getString(numberIndex) ?: ""
                    fetchedContacts.add(Contact(name, number))
                }
            }
            // Remove duplicates (same name and number)
            contacts = fetchedContacts.distinctBy { it.name + it.number }
        }
    }

    if (!permissionState.status.isGranted) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
            Text("Contacts permission is required to view contacts.")
            Button(onClick = { permissionState.launchPermissionRequest() }, modifier = Modifier.padding(top = 16.dp)) {
                Text("Grant Permission")
            }
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(contacts) { contact ->
            ListItem(
                headlineContent = { Text(contact.name, fontWeight = FontWeight.Bold) },
                supportingContent = { Text(contact.number) },
            )
            Divider()
        }
    }
}
