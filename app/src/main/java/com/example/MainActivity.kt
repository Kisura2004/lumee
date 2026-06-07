package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.LumeeViewModel
import com.example.ui.screens.GreetingScreen
import com.example.ui.screens.QuotesScreen
import com.example.ui.screens.ReflectionsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                LumeeApp()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LumeeApp(viewModel: LumeeViewModel = viewModel()) {
    val activeScreen by viewModel.activeScreen.collectAsState()

    // Request Notification permission on App Start (Android 13+)
    val context = androidx.compose.ui.platform.LocalContext.current
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Handle notification permission result
    }

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= 33) { // Build.VERSION_CODES.TIRAMISU
            val permissionCheck = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                "android.permission.POST_NOTIFICATIONS" // Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch("android.permission.POST_NOTIFICATIONS")
            }
        }
    }

    // Determine Frosted Glass premium theme background gradient
    val backgroundBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFFDF6F0), // Soft warm cream
                Color(0xFFF4E8E8), // Soft lavender-grey-blush
                Color(0xFFE8F0F7)  // Soft ice white-blue
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent, // Let the beautiful frosted glass gradient act as canvas
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("lumee_bottom_bar"),
                    containerColor = Color.White.copy(alpha = 0.15f), // Semi-transparent frosted bottom nav bar base
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = activeScreen == LumeeViewModel.Screen.TODAY,
                        onClick = { viewModel.navigateTo(LumeeViewModel.Screen.TODAY) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Today's light"
                            )
                        },
                        label = { Text("Today") },
                        modifier = Modifier.testTag("nav_item_today"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF3D3834),
                            selectedTextColor = Color(0xFF3D3834),
                            unselectedIconColor = Color(0xFF3D3834).copy(alpha = 0.4f),
                            unselectedTextColor = Color(0xFF3D3834).copy(alpha = 0.4f),
                            indicatorColor = Color.White.copy(alpha = 0.3f)
                        )
                    )

                    NavigationBarItem(
                        selected = activeScreen == LumeeViewModel.Screen.REFLECTIONS,
                        onClick = { viewModel.navigateTo(LumeeViewModel.Screen.REFLECTIONS) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Create,
                                contentDescription = "Private Reflections"
                            )
                        },
                        label = { Text("Thoughts") },
                        modifier = Modifier.testTag("nav_item_thoughts"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF3D3834),
                            selectedTextColor = Color(0xFF3D3834),
                            unselectedIconColor = Color(0xFF3D3834).copy(alpha = 0.4f),
                            unselectedTextColor = Color(0xFF3D3834).copy(alpha = 0.4f),
                            indicatorColor = Color.White.copy(alpha = 0.3f)
                        )
                    )

                    NavigationBarItem(
                        selected = activeScreen == LumeeViewModel.Screen.QUOTES,
                        onClick = { viewModel.navigateTo(LumeeViewModel.Screen.QUOTES) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Quotes Sanctuary"
                            )
                        },
                        label = { Text("Quotes") },
                        modifier = Modifier.testTag("nav_item_quotes"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF3D3834),
                            selectedTextColor = Color(0xFF3D3834),
                            unselectedIconColor = Color(0xFF3D3834).copy(alpha = 0.4f),
                            unselectedTextColor = Color(0xFF3D3834).copy(alpha = 0.4f),
                            indicatorColor = Color.White.copy(alpha = 0.3f)
                        )
                    )

                    NavigationBarItem(
                        selected = activeScreen == LumeeViewModel.Screen.SETTINGS,
                        onClick = { viewModel.navigateTo(LumeeViewModel.Screen.SETTINGS) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Preferences") },
                        modifier = Modifier.testTag("nav_item_settings"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF3D3834),
                            selectedTextColor = Color(0xFF3D3834),
                            unselectedIconColor = Color(0xFF3D3834).copy(alpha = 0.4f),
                            unselectedTextColor = Color(0xFF3D3834).copy(alpha = 0.4f),
                            indicatorColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        ) { innerPadding ->
            // Animated screen transition
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = activeScreen,
                    transitionSpec = {
                        fadeIn() with fadeOut()
                    },
                    modifier = Modifier.fillMaxSize()
                ) { screen ->
                    when (screen) {
                        LumeeViewModel.Screen.TODAY -> {
                            GreetingScreen(
                                viewModel = viewModel,
                                onNavigateToReflections = {
                                    viewModel.navigateTo(LumeeViewModel.Screen.REFLECTIONS)
                                }
                            )
                        }
                        LumeeViewModel.Screen.REFLECTIONS -> {
                            ReflectionsScreen(viewModel = viewModel)
                        }
                        LumeeViewModel.Screen.QUOTES -> {
                            QuotesScreen(viewModel = viewModel)
                        }
                        LumeeViewModel.Screen.SETTINGS -> {
                            SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
