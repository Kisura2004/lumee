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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.LumeeViewModel
import com.example.ui.screens.GreetingScreen
import com.example.ui.screens.QuotesScreen
import com.example.ui.screens.ReflectionsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import java.util.Calendar

class MainActivity : ComponentActivity() {
    private val currentIntentState = mutableStateOf<android.content.Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        currentIntentState.value = intent
        setContent {
            MyApplicationTheme {
                LumeeApp(intent = currentIntentState.value)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntentState.value = intent
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LumeeApp(intent: android.content.Intent?, viewModel: LumeeViewModel = viewModel()) {
    val activeScreen by viewModel.activeScreen.collectAsState()

    LaunchedEffect(intent) {
        if (intent != null) {
            viewModel.handleNotificationIntent(intent)
        }
    }

    // Request Notification permission on App Start (Android 13+)
    val context = androidx.compose.ui.platform.LocalContext.current
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Handle notification permission result
    }

    LaunchedEffect(Unit) {
        viewModel.checkAndTriggerStartupGreeting()
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

        val showStartupGreeting by viewModel.showStartupGreeting.collectAsState()
        androidx.compose.animation.AnimatedVisibility(
            visible = showStartupGreeting != null,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            showStartupGreeting?.let { type ->
                StartupGreetingOverlay(
                    type = type,
                    viewModel = viewModel,
                    onDismiss = { viewModel.dismissStartupGreeting() }
                )
            }
        }
    }
}

@Composable
fun StartupGreetingOverlay(
    type: LumeeViewModel.StartupGreetingType,
    viewModel: LumeeViewModel,
    onDismiss: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val name = userProfile?.name ?: "Friend"

    val isMorning = type == LumeeViewModel.StartupGreetingType.MORNING

    val fontColor = if (isMorning) Color(0xFF3D3834) else Color(0xFFECE5DF)
    val subFontColor = if (isMorning) Color(0xFF3D3834).copy(alpha = 0.6f) else Color(0xFFECE5DF).copy(alpha = 0.7f)

    val backgroundBrush = if (isMorning) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFF9E8), // Buttery gold
                Color(0xFFFBEBE6), // Dawn peach
                Color(0xFFF2EAF1)  // Lilac cream
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F101A), // Midnight black indigo
                Color(0xFF1D1B30), // Deep purple space
                Color(0xFF15101F)  // Warm obsidian dark berry
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    if (isMorning) {
                        drawCircle(
                            color = Color(0xFFFFD180).copy(alpha = 0.15f),
                            radius = 450f,
                            center = Offset(size.width * 0.7f, size.height * 0.25f)
                        )
                        drawCircle(
                            color = Color(0xFFFF8A80).copy(alpha = 0.08f),
                            radius = 600f,
                            center = Offset(size.width * 0.2f, size.height * 0.75f)
                        )
                    } else {
                        drawCircle(
                            color = Color(0xFF7E57C2).copy(alpha = 0.15f),
                            radius = 500f,
                            center = Offset(size.width * 0.8f, size.height * 0.3f)
                        )
                        drawCircle(
                            color = Color(0xFF3F51B5).copy(alpha = 0.12f),
                            radius = 600f,
                            center = Offset(size.width * 0.15f, size.height * 0.8f)
                        )
                    }
                }
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(
                    width = 1.dp,
                    color = if (isMorning) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(32.dp)
                ),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isMorning) Color.White.copy(alpha = 0.35f) else Color(0xFF1F2036).copy(alpha = 0.45f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = if (isMorning) Color(0xFFFFF176).copy(alpha = 0.25f) else Color(0xFFFFD54F).copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isMorning) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Sunrise icon",
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .drawBehind {
                                    drawCircle(
                                        color = Color(0xFFFFD54F),
                                        radius = size.width / 3f
                                    )
                                }
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isMorning) "Morning Wakeup 🌅" else "Late Night Motivation 🌌",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = if (isMorning) Color(0xFFE0A7A7) else Color(0xFFFFCCD5)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isMorning) "Rise and shine, $name" else "Quiet under the stars, $name",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = fontColor
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                val quoteText = if (isMorning) {
                    "Today is a playground of possibilities. Let your curiosity lead the way, and find joy in the smallest discoveries."
                } else {
                    "Your inner heart quietly restores the weights of today. Rest fully, release expectation, and dream of gentle roads."
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isMorning) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "“$quoteText”",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic,
                            color = fontColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Text(
                    text = if (isMorning) "How would you like to breathe into today?" else "How would you like to gently wind down?",
                    fontSize = 13.sp,
                    color = subFontColor,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (isMorning) {
                                viewModel.setPreferredBreathingTechnique("BOX")
                            } else {
                                viewModel.setPreferredBreathingTechnique("CALM")
                            }
                            viewModel.setTriggerBreathing(true)
                            viewModel.navigateTo(LumeeViewModel.Screen.TODAY)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag(if (isMorning) "morning_breathe_btn" else "evening_breathe_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMorning) Color(0xFFE0A7A7) else Color(0xFF7E57C2)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isMorning) "Quick Daily Inhale" else "Relax & Unwind Calm",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            if (isMorning) {
                                viewModel.setTriggerComposer(true)
                                viewModel.navigateTo(LumeeViewModel.Screen.REFLECTIONS)
                            } else {
                                viewModel.navigateTo(LumeeViewModel.Screen.QUOTES)
                            }
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag(if (isMorning) "morning_journal_btn" else "evening_quotes_btn"),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isMorning) Color(0xFF3D3834).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.3f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = fontColor
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isMorning) Icons.Default.Create else Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = fontColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isMorning) "Unload Morning Thoughts" else "Enter Quotes Sanctuary",
                                fontWeight = FontWeight.Bold,
                                color = fontColor
                            )
                        }
                    }

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dismiss_startup_greeting_btn"),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = subFontColor
                        )
                    ) {
                        Text(
                            text = if (isMorning) "Step gently into today →" else "Rest beautifully under the stars →",
                            fontWeight = FontWeight.SemiBold,
                            color = subFontColor
                        )
                    }
                }
            }
        }
    }
}
