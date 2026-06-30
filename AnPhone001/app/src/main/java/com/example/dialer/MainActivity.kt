package com.example.dialer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DialerAppTheme {
                DialerApp()
            }
        }
    }
}

@Composable
fun DialerAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        content = content
    )
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dialer : Screen("dialer", "Keypad", Icons.Filled.Phone)
    object Recents : Screen("recents", "Recents", Icons.Filled.Call)
    object Contacts : Screen("contacts", "Contacts", Icons.Filled.Person)
    object CallScreen : Screen("call_screen", "Call", Icons.Filled.Phone)
}

@Composable
fun DialerApp() {
    val navController = rememberNavController()

    val items = listOf(
        Screen.Dialer,
        Screen.Recents,
        Screen.Contacts
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            // Hide bottom bar on the CallScreen
            if (currentRoute != Screen.CallScreen.route) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Dialer.route, Modifier.padding(innerPadding)) {
            composable(Screen.Dialer.route) { DialerScreen(navController) }
            composable(Screen.Recents.route) { RecentsScreen() }
            composable(Screen.Contacts.route) { ContactsScreen() }
            composable(Screen.CallScreen.route) { CallScreen(navController) }
        }
    }
}
