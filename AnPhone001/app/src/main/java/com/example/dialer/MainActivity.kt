package com.example.dialer

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    private val setDialerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
        // Handle result if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Full edge-to-edge support
        requestDefaultDialerRole()

        handleIntent(intent)

        setContent {
            DialerAppTheme {
                val navController = rememberNavController()

                // Observe CallManager to auto-navigate to CallScreen
                LaunchedEffect(Unit) {
                    CallManager.callState.collectLatest { wrapper ->
                        val call = wrapper.call
                        if (call != null && wrapper.state != android.telecom.Call.STATE_DISCONNECTED) {
                            if (navController.currentDestination?.route != Screen.CallScreen.route) {
                                navController.navigate(Screen.CallScreen.route) {
                                    launchSingleTop = true
                                }
                            }
                        } else if (call == null || wrapper.state == android.telecom.Call.STATE_DISCONNECTED) {
                            if (navController.currentDestination?.route == Screen.CallScreen.route) {
                                navController.popBackStack(Screen.Dialer.route, false)
                            }
                        }
                    }
                }

                DialerApp(navController)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent?.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == "ACTION_ANSWER_CALL") {
            val call = CallManager.callState.value.call
            if (call != null && call.state == android.telecom.Call.STATE_RINGING) {
                call.answer(call.details.videoState)
            }
            // The notification should be dismissed when the app opens
            NotificationHelper.cancelNotification(this)
        }
    }

    private fun requestDefaultDialerRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                setDialerLauncher.launch(intent)
            }
        } else {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            if (telecomManager.defaultDialerPackage != packageName) {
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                    .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                setDialerLauncher.launch(intent)
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
    object Dialer : Screen("dialer", "Recent", Icons.Filled.Phone)
    object Contacts : Screen("contacts", "Contact", Icons.Filled.Person)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
    object Transfer : Screen("transfer", "Trans", Icons.Filled.SyncAlt)
    object CallScreen : Screen("call_screen", "Call", Icons.Filled.Phone)
}

@Composable
fun DialerApp(navController: NavHostController) {
    val items = listOf(
        Screen.Dialer,
        Screen.Contacts,
        Screen.Settings,
        Screen.Transfer
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Explicitly hoist keypad state here to fix navigation bar disappear issue reliably
    var isKeypadVisible by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            // Show bottom bar only on top-level screens and when keypad is NOT visible
            val showBottomBar = !isKeypadVisible && (currentRoute in items.map { it.route })
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(250)) + fadeIn(animationSpec = tween(250)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(250)) + fadeOut(animationSpec = tween(250))
            ) {
                FloatingNavigationBar(
                    items = items,
                    currentRoute = currentRoute,
                    onItemClick = { screen ->
                        isKeypadVisible = false // Close keypad when switching tabs
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
    ) { innerPadding ->
        val modifier = Modifier

        NavHost(navController, startDestination = Screen.Dialer.route, modifier) {
            composable(Screen.Dialer.route) {
                DialerScreen(
                    navController = navController,
                    innerPadding = innerPadding,
                    isKeypadVisible = isKeypadVisible,
                    onKeypadVisibilityChange = { visible -> isKeypadVisible = visible }
                )
            }
            composable(Screen.Contacts.route) { ContactsScreen(innerPadding = innerPadding) }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(Screen.Transfer.route) { TransferScreen() }
            composable(Screen.CallScreen.route) { CallScreen(navController) }
            composable(
                route = "call_detail/{number}/{name}"
            ) { backStackEntry ->
                val number = backStackEntry.arguments?.getString("number") ?: ""
                val name = backStackEntry.arguments?.getString("name") ?: ""
                // To display CallDetailScreen, we can reconstruct the recent logs or fetch them.
                // Since this is a simple UX flow, we can just display the UI. Ideally we'd pass a SharedViewModel.
                // For now, we will just fetch the specific logs in the Composable or pass the basics.
                CallDetailScreenWrapper(navController, number, name)
            }
        }
    }
}

@Composable
fun FloatingNavigationBar(
    items: List<Screen>,
    currentRoute: String?,
    onItemClick: (Screen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Removed heavy 'blur' modifier for low-end phone optimization
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f) // Optimized transparent look
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { screen ->
                    val selected = currentRoute == screen.route
                    val backgroundColor by animateColorAsState(
                        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        animationSpec = tween(durationMillis = 200) // Faster animation for low-end
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(durationMillis = 200)
                    )

                    val itemWeight by animateFloatAsState(
                        targetValue = if (selected) 2f else 1f,
                        animationSpec = tween(durationMillis = 200)
                    )

                    Row(
                        modifier = Modifier
                            .weight(itemWeight)
                            .clip(CircleShape)
                            .background(backgroundColor)
                            .clickable { onItemClick(screen) }
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title,
                            tint = contentColor
                        )
                        if (selected) {
                            Text(
                                text = screen.title,
                                modifier = Modifier.padding(start = 4.dp),
                                color = contentColor,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
