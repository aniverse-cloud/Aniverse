package com.anverse.phone

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.anverse.phone.ui.screens.MainDashboardLayout
import com.anverse.phone.ui.theme.AnverseTheme
import com.anverse.phone.viewmodel.DialerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: DialerViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            viewModel.fetchContacts()
            viewModel.fetchCallLogs()
        } else {
            Toast.makeText(this, "Anverse requires full authorizations to function.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkDefaultDialerRole()

        setContent {
            AnverseTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainDashboardLayout(viewModel)
                }
            }
        }

        requestPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.CALL_PHONE,
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.READ_CALL_LOG,
                android.Manifest.permission.RECORD_AUDIO
            )
        )
    }

    private fun checkDefaultDialerRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                startActivityForResult(intent, 101)
            }
        } else {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            if (telecomManager.defaultDialerPackage != packageName) {
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                    putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                }
                startActivity(intent)
            }
        }
    }
}
