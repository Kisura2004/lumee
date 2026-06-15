package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.CircleShape
import com.example.ui.LumeeViewModel
import java.util.Calendar

enum class BreathingPhase { IDLE, INHALE, HOLD_IN, EXHALE, HOLD_OUT }

enum class BreathingTechnique(
    val title: String,
    val description: String,
    val inhaleMs: Long,
    val holdInMs: Long,
    val exhaleMs: Long,
    val holdOutMs: Long,
    val inhaleSec: Int,
    val holdInSec: Int,
    val exhaleSec: Int,
    val holdOutSec: Int
) {
    BOX("Box Breath", "Cognitive clarity & sharp focus", 4000, 4000, 4000, 4000, 4, 4, 4, 4),
    CALM("4-7-8 Relax", "Nervous system reset & stress relief", 4000, 7000, 8000, 0, 4, 7, 8, 0),
    COHERENT("Balanced", "Settle heart rhythm & balance mood", 5000, 0, 5000, 0, 5, 0, 5, 0)
}

@Composable
fun GreetingScreen(
    viewModel: LumeeViewModel,
    onNavigateToReflections: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todayGreeting by viewModel.todayGreeting.collectAsState()
    val isGreetingLoading by viewModel.isGreetingLoading.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val emotionsList by viewModel.emotions.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    var showProfileModal by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var isAuthenticated by remember { mutableStateOf(false) }

    var newMonthInput by remember { mutableStateOf("") }
    var newDayInput by remember { mutableStateOf("") }
    var newNameInput by remember { mutableStateOf("") }
    var plainPasswordInput by remember { mutableStateOf("") }

    // Init values once profile loads
    LaunchedEffect(userProfile, showProfileModal) {
        userProfile?.let {
            newMonthInput = it.birthMonth?.toString() ?: ""
            newDayInput = it.birthDay?.toString() ?: ""
            newNameInput = it.name
            plainPasswordInput = it.password ?: ""
            if (it.password == null || it.password.isEmpty()) {
                isAuthenticated = true
            } else {
                isAuthenticated = false
            }
        }
    }

    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val isMorning = hour < 11

    val weekdayFormat = remember { java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault()) }
    val dateFormat = remember { java.text.SimpleDateFormat("MMMM d", java.util.Locale.getDefault()) }
    val weekdayString = weekdayFormat.format(calendar.time)
    val dateString = dateFormat.format(calendar.time)

    val greetingText = todayGreeting?.first ?: "The day is still yours. Take a slow breath, and begin again where you are."
    val categoryLabel = todayGreeting?.second ?: "Morning Reflection"
    val profileName = userProfile?.name ?: "Friend"
    val streak = userProfile?.streakCount ?: 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // High-fidelity Frosted Glass Header Row (mimicking Design HTML)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Date cluster with Clickable Avatar Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = { showProfileModal = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.45f))
                        .testTag("launch_profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Access Profile",
                        tint = Color(0xFF3D3834).copy(alpha = 0.65f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = weekdayString.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color(0xFF3D3834).copy(alpha = 0.4f)
                    )
                    Text(
                        text = dateString,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF3D3834).copy(alpha = 0.7f)
                    )
                }
            }

            // Right Streak Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color.White.copy(alpha = 0.4f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(100.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                // Streak dot indicator in pink blush
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color(0xFFE0A7A7))
                )
                Text(
                    text = if (streak > 0) "$streak days of light" else "light space",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3D3834)
                )
            }
        }

        // Welcome text greeting card headline
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            val titleMsg = if (isMorning) {
                "Good morning, $profileName"
            } else {
                "A peaceful afternoon, $profileName"
            }
            Text(
                text = titleMsg,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3D3834).copy(alpha = 0.85f)
                ),
                textAlign = TextAlign.Center
            )
        }

        // Beautiful Luminous Greeting Main Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White.copy(alpha = 0.2f))  // White 20% opacity
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.6f),  // Border-white/60
                    shape = RoundedCornerShape(32.dp)
                )
                .drawBehind {
                    // Soft glowing orange circle top-right
                    drawCircle(
                        color = Color(0xFFFFCC80).copy(alpha = 0.12f),
                        radius = 220f,
                        center = Offset(size.width - 20f, -10f)
                    )
                    // Soft glowing blue circle bottom-left
                    drawCircle(
                        color = Color(0xFF90CAF9).copy(alpha = 0.12f),
                        radius = 220f,
                        center = Offset(20f, size.height + 10f)
                    )
                }
                .padding(28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Minimalist horizontal elegant divider
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(2.dp)
                        .background(Color(0xFFE0A7A7).copy(alpha = 0.5f))
                        .padding(bottom = 0.dp)
                )

                // Greeting quote
                if (isGreetingLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 32.dp),
                        color = Color(0xFFE0A7A7)
                    )
                } else {
                    Text(
                        text = "“$greetingText”",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF3D3834),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Bottom spacer with category label context
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Chip
                    Text(
                        text = categoryLabel.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF3D3834).copy(alpha = 0.5f)
                    )

                    // Actions Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        // Copy Button
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Lumee Contemplation", greetingText)
                                clipboard.setPrimaryClip(clip)
                                android.widget.Toast.makeText(context, "Copied quote to clipboard ✨", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .testTag("copy_greeting_button")
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Contemplation",
                                tint = Color(0xFF3D3834).copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Share Button (Beautifully creates nice image)
                        IconButton(
                            onClick = {
                                shareQuoteAsImage(context, greetingText, categoryLabel)
                            },
                            modifier = Modifier
                                .testTag("share_greeting_button")
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Contemplation",
                                tint = Color(0xFF3D3834).copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Refresh Button (Quiet & Soft)
                        IconButton(
                            onClick = { viewModel.refreshTodayGreeting(force = true) },
                            modifier = Modifier
                                .testTag("refresh_greeting_button")
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Greeting",
                                tint = Color(0xFF3D3834).copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Aesthetic Dynamic Breathing Companion
        var isBreathingActive by remember { mutableStateOf(false) }
        var breathingPhase by remember { mutableStateOf(BreathingPhase.IDLE) }
        var selectedTechnique by remember { mutableStateOf(BreathingTechnique.BOX) }
        var secondsRemaining by remember { mutableStateOf(0) }

        val triggerBreathingState by viewModel.triggerBreathing.collectAsState()
        val preferredTechniqueState by viewModel.preferredBreathingTechnique.collectAsState()

        LaunchedEffect(triggerBreathingState) {
            if (triggerBreathingState) {
                if (preferredTechniqueState == "CALM") {
                    selectedTechnique = BreathingTechnique.CALM
                } else if (preferredTechniqueState == "COHERENT") {
                    selectedTechnique = BreathingTechnique.COHERENT
                } else {
                    selectedTechnique = BreathingTechnique.BOX
                }
                isBreathingActive = true
                viewModel.setTriggerBreathing(false)
                viewModel.setPreferredBreathingTechnique(null)
            }
        }

        LaunchedEffect(isBreathingActive, selectedTechnique) {
            if (!isBreathingActive) {
                breathingPhase = BreathingPhase.IDLE
                secondsRemaining = 0
                return@LaunchedEffect
            }
            while (true) {
                // Inhale phase
                breathingPhase = BreathingPhase.INHALE
                for (s in selectedTechnique.inhaleSec downTo 1) {
                    secondsRemaining = s
                    kotlinx.coroutines.delay(1000)
                }

                // Hold In phase
                if (selectedTechnique.holdInSec > 0) {
                    breathingPhase = BreathingPhase.HOLD_IN
                    for (s in selectedTechnique.holdInSec downTo 1) {
                        secondsRemaining = s
                        kotlinx.coroutines.delay(1000)
                    }
                }

                // Exhale phase
                breathingPhase = BreathingPhase.EXHALE
                for (s in selectedTechnique.exhaleSec downTo 1) {
                    secondsRemaining = s
                    kotlinx.coroutines.delay(1000)
                }

                // Hold Out phase
                if (selectedTechnique.holdOutSec > 0) {
                    breathingPhase = BreathingPhase.HOLD_OUT
                    for (s in selectedTechnique.holdOutSec downTo 1) {
                        secondsRemaining = s
                        kotlinx.coroutines.delay(1000)
                    }
                }
            }
        }

        val scaleTarget = when (breathingPhase) {
            BreathingPhase.IDLE -> 1.0f
            BreathingPhase.INHALE -> 1.7f
            BreathingPhase.HOLD_IN -> 1.7f
            BreathingPhase.EXHALE -> 1.0f
            BreathingPhase.HOLD_OUT -> 1.0f
        }
        val textLabel = when (breathingPhase) {
            BreathingPhase.IDLE -> "Begin"
            BreathingPhase.INHALE -> "Breathe In"
            BreathingPhase.HOLD_IN -> "Hold"
            BreathingPhase.EXHALE -> "Breathe Out"
            BreathingPhase.HOLD_OUT -> "Rest"
        }
        val durationMillisValue = when (breathingPhase) {
            BreathingPhase.INHALE -> selectedTechnique.inhaleMs.toInt()
            BreathingPhase.EXHALE -> selectedTechnique.exhaleMs.toInt()
            else -> 1000
        }
        val animatedScale by androidx.compose.animation.core.animateFloatAsState(
            targetValue = scaleTarget,
            animationSpec = androidx.compose.animation.core.tween(
                durationMillis = durationMillisValue,
                easing = androidx.compose.animation.core.LinearEasing
            ),
            label = "BreathingScale"
        )

        val colorTarget = when (breathingPhase) {
            BreathingPhase.IDLE -> Color(0xFFE0A7A7).copy(alpha = 0.15f)
            BreathingPhase.INHALE -> Color(0xFFFEF3C7).copy(alpha = 0.4f)  // Sunbeam Amber
            BreathingPhase.HOLD_IN -> Color(0xFF90CAF9).copy(alpha = 0.35f) // Deep sky blue
            BreathingPhase.EXHALE -> Color(0xFFC084FC).copy(alpha = 0.35f) // Evening Lavender
            BreathingPhase.HOLD_OUT -> Color(0xFFE0A7A7).copy(alpha = 0.2f)
        }
        val animatedColor by androidx.compose.animation.animateColorAsState(
            targetValue = colorTarget,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000),
            label = "BreathingColor"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White.copy(alpha = 0.25f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with subtitle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "LUMEE BREATH SPACE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color(0xFFE0A7A7)
                    )
                    Text(
                        text = "Calm your mind using ${selectedTechnique.title}.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF3D3834).copy(alpha = 0.6f)
                    )
                    Text(
                        text = selectedTechnique.description,
                        fontSize = 10.sp,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF3D3834).copy(alpha = 0.4f)
                    )
                }

                // Technique Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BreathingTechnique.values().forEach { tech ->
                        val isSelected = selectedTechnique == tech
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) Color(0xFFE0A7A7).copy(alpha = 0.25f)
                                    else Color.White.copy(alpha = 0.15f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFFE0A7A7) else Color.White.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedTechnique = tech
                                    isBreathingActive = false
                                    breathingPhase = BreathingPhase.IDLE
                                    secondsRemaining = 0
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = tech.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF8B5A5A) else Color(0xFF3D3834)
                                )
                                Text(
                                    text = "${tech.inhaleSec}-${tech.holdInSec}-${tech.exhaleSec}${if (tech.holdOutSec > 0) "-${tech.holdOutSec}" else ""}",
                                    fontSize = 9.sp,
                                    color = if (isSelected) Color(0xFF8B5A5A).copy(alpha = 0.7f) else Color(0xFF3D3834).copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                // Breathing Sphere Container
                Box(
                    modifier = Modifier
                        .size(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing Glow sphere
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .graphicsLayer {
                                scaleX = animatedScale
                                scaleY = animatedScale
                            }
                            .clip(CircleShape)
                            .background(animatedColor)
                            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                    )

                    // Outer border guideline
                    Box(
                        modifier = Modifier
                            .size(119.dp)
                            .border(0.75.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                    )

                    // Phase Text Label inside bubble
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = textLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF3D3834),
                            textAlign = TextAlign.Center
                        )
                        if (isBreathingActive && secondsRemaining > 0) {
                            Text(
                                text = "${secondsRemaining}s",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3D3834).copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // Start/Pause Button
                Button(
                    onClick = { isBreathingActive = !isBreathingActive },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBreathingActive) Color(0xFF3D3834).copy(alpha = 0.1f) else Color(0xFFE0A7A7),
                        contentColor = if (isBreathingActive) Color(0xFF3D3834) else Color.White
                    ),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("toggle_breathing_button")
                ) {
                    Text(
                        text = if (isBreathingActive) "Pause Exercise" else "Begin ${selectedTechnique.title}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Streak Section - Celebrating consistency beautifully in frosted style
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.3f))  // White 30% background
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Container with 20% opacity pink highlight background
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE0A7A7).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (streak > 0) "🔥" else "🌱",
                        fontSize = 20.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    val titleLine = if (streak > 1) {
                        "$streak Consecutive Days"
                    } else if (streak == 1) {
                        "A beautiful start"
                    } else {
                        "A quiet beginning"
                    }

                    Text(
                        text = titleLine,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF3D3834)
                    )

                    val statusMsg = if (streak > 1) {
                        "Your light shines consistently. Keep taking slow, mindful steps."
                    } else if (streak == 1) {
                        "Your small commitment today is a seed for tomorrow's calm."
                    } else {
                        "No pressure. Today is a fresh clean slate. Let's begin."
                    }
                    Text(
                        text = statusMsg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF3D3834).copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Quick Access to Reflections in matching Soft Glass Row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.3f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(24.dp)
                )
                .clickable { onNavigateToReflections() }
                .testTag("quick_reflection_card")
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Elegant rose highlight background
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE0A7A7).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Create,
                            contentDescription = "Pen icon",
                            tint = Color(0xFFE0A7A7),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Quiet Thoughts",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF3D3834)
                        )
                        Text(
                            text = "Capture a moment for your future self",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF3D3834).copy(alpha = 0.5f)
                        )
                    }
                }

                Text(
                    text = "→",
                    fontSize = 20.sp,
                    color = Color(0xFFE0A7A7),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✦ INTERACTIVE CHAT BUDDY ✦
        var emotionInputText by remember { mutableStateOf("") }
        val coroutineScope = rememberCoroutineScope()
        val listState = androidx.compose.foundation.lazy.rememberLazyListState()

        var isChatUnlocked by remember { mutableStateOf(false) }
        var chatUnlockPasswordInput by remember { mutableStateOf("") }
        val savedPassword = userProfile?.password

        // Sync scroll to end when new messages arrive
        LaunchedEffect(emotionsList.size) {
            if (emotionsList.isNotEmpty()) {
                listState.animateScrollToItem(emotionsList.size - 1)
            }
        }

        // Automatic state adjustment based on password settings
        LaunchedEffect(savedPassword) {
            if (savedPassword.isNullOrEmpty()) {
                isChatUnlocked = true
            } else {
                isChatUnlocked = false
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.35f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header of Buddy Chat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFE0A7A7).copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌸", fontSize = 16.sp)
                        }
                        Column {
                            Text(
                                text = "Lumee Chat Buddy",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF3D3834)
                            )
                            Text(
                                text = "Share your raw emotions securely & locally",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF3D3834).copy(alpha = 0.45f)
                            )
                        }
                    }

                    if (!savedPassword.isNullOrEmpty() && isChatUnlocked) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE0A7A7).copy(alpha = 0.15f))
                                .clickable {
                                    isChatUnlocked = false
                                    chatUnlockPasswordInput = ""
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Lock", fontSize = 11.sp, color = Color(0xFF8B5A5A), fontWeight = FontWeight.Bold)
                                Text("🔒", fontSize = 11.sp)
                            }
                        }
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.4f), thickness = 0.8.dp)

                if (!isChatUnlocked) {
                    // Password lock overlay for local privacy compliance
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Secure Companion Shield 🔒",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B5A5A),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "To view your emotional companion records and continue chatting with Lumee, please enter your offline sanctuary password.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF3D3834).copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        OutlinedTextField(
                            value = chatUnlockPasswordInput,
                            onValueChange = { chatUnlockPasswordInput = it },
                            placeholder = { Text("Enter your profile password") },
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE0A7A7),
                                unfocusedBorderColor = Color(0xFF3D3834).copy(alpha = 0.2f),
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )

                        Button(
                            onClick = {
                                if (chatUnlockPasswordInput == savedPassword) {
                                    isChatUnlocked = true
                                    chatUnlockPasswordInput = ""
                                } else {
                                    android.widget.Toast.makeText(context, "Incorrect Password", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0A7A7)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        ) {
                            Text("Unlock Conversations 🌸", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                } else {
                    // Dialog lists
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 220.dp)
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                            .padding(8.dp)
                    ) {
                        if (emotionsList.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "How are you truly feeling right now?\nType below or select a mood chip...",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF3D3834).copy(alpha = 0.5f)
                                )
                            }
                        } else {
                            androidx.compose.foundation.lazy.LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(emotionsList) { msg ->
                                    val isUser = msg.sender == "USER"
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .widthIn(max = 240.dp)
                                                .clip(
                                                    RoundedCornerShape(
                                                        topStart = 16.dp,
                                                        topEnd = 16.dp,
                                                        bottomStart = if (isUser) 16.dp else 4.dp,
                                                        bottomEnd = if (isUser) 4.dp else 16.dp
                                                    )
                                                )
                                                .background(
                                                    if (isUser) Color(0xFFE0A7A7).copy(alpha = 0.8f)
                                                    else Color.White.copy(alpha = 0.65f)
                                                )
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = msg.text,
                                                fontSize = 13.sp,
                                                color = if (isUser) Color.White else Color(0xFF3D3834)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Mood quick chips to tap
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val moodChips = listOf(
                            "😔 Sad",
                            "😰 Anxious",
                            "😡 Angry",
                            "😃 Happy"
                        )
                        moodChips.forEach { chipName ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.4f))
                                    .clickable {
                                        viewModel.sendEmotion("I am feeling $chipName right now.")
                                    }
                                    .border(0.6.dp, Color(0xFFE0A7A7).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(text = chipName, fontSize = 11.sp, color = Color(0xFF8B5A5A))
                            }
                        }
                    }

                    // Type bar input field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = emotionInputText,
                            onValueChange = { emotionInputText = it },
                            placeholder = { Text("Share an emotion...", fontSize = 13.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.45f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.25f),
                                focusedBorderColor = Color(0xFFE0A7A7),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )

                        Button(
                            onClick = {
                                if (emotionInputText.trim().isNotEmpty()) {
                                    viewModel.sendEmotion(emotionInputText)
                                    emotionInputText = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0A7A7)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Send", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showProfileModal) {
        Dialog(onDismissRequest = { 
            showProfileModal = false
            passwordInput = ""
        }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFFFFF9F5))
                    .border(1.6.dp, Color(0xFFE0A7A7), RoundedCornerShape(28.dp))
                    .padding(22.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your Private Sanctuary Profile",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF8B5A5A)
                    )

                    val savedPassword = userProfile?.password

                    if (!isAuthenticated && savedPassword != null && savedPassword.isNotEmpty()) {
                        // Password protection prompt
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "This profile is locked. Please enter your offline password to access your secure local recordings.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF3D3834).copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )

                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Enter Password") },
                                shape = RoundedCornerShape(14.dp),
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFE0A7A7),
                                    unfocusedBorderColor = Color(0xFF3D3834).copy(alpha = 0.2f),
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                )
                            )

                            Button(
                                onClick = {
                                    if (passwordInput == savedPassword) {
                                        isAuthenticated = true
                                    } else {
                                        android.widget.Toast.makeText(context, "Incorrect Password", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0A7A7)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Unlock Profile 🔑", color = Color.White)
                            }
                        }
                    } else {
                        // Authenticated details panel!
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 380.dp)
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Birthday, Name, and Password Setup block
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.6f))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "Your Personal Identity 🌸",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF8B5A5A)
                                    )

                                    // Name input
                                    OutlinedTextField(
                                        value = newNameInput,
                                        onValueChange = { newNameInput = it },
                                        label = { Text("What should Lumee call you?", fontSize = 11.sp) },
                                        placeholder = { Text("Friend") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFE0A7A7),
                                            focusedTextColor = Color.Black,
                                            unfocusedTextColor = Color.Black
                                        )
                                    )

                                    // Birthday inputs
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = newMonthInput,
                                            onValueChange = { newMonthInput = it.take(2) },
                                            label = { Text("Birth Month (1-12)", fontSize = 11.sp) },
                                            placeholder = { Text("MM") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFFE0A7A7),
                                                focusedTextColor = Color.Black,
                                                unfocusedTextColor = Color.Black
                                            )
                                        )
                                        OutlinedTextField(
                                            value = newDayInput,
                                            onValueChange = { newDayInput = it.take(2) },
                                            label = { Text("Birth Day (1-31)", fontSize = 11.sp) },
                                            placeholder = { Text("DD") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFFE0A7A7),
                                                focusedTextColor = Color.Black,
                                                unfocusedTextColor = Color.Black
                                            )
                                        )
                                    }

                                    // Password setup
                                    OutlinedTextField(
                                        value = plainPasswordInput,
                                        onValueChange = { plainPasswordInput = it },
                                        label = { Text("Set Offline Profile Password", fontSize = 11.sp) },
                                        placeholder = { Text("Keep your sanctuary private") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFE0A7A7),
                                            focusedTextColor = Color.Black,
                                            unfocusedTextColor = Color.Black
                                        )
                                    )

                                    Button(
                                        onClick = {
                                            val m = newMonthInput.toIntOrNull()
                                            val d = newDayInput.toIntOrNull()
                                            viewModel.updateProfileName(newNameInput.ifEmpty { "Friend" })
                                            viewModel.updateProfileBirthday(m, d)
                                            viewModel.updatePassword(plainPasswordInput.ifEmpty { null })
                                            android.widget.Toast.makeText(context, "Identity Updated Successfully 🌸", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0A7A7)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Save Sincere Identity", color = Color.White)
                                    }
                                }
                            }

                            // Emotions & Chat Buddy Responses Recipient Panel
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Saved Emotions & Buddy Chats",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF8B5A5A)
                                    )
                                    if (emotionsList.isNotEmpty()) {
                                        Text(
                                            text = "Clear Chats",
                                            fontSize = 11.sp,
                                            color = Color(0xFF8B5A5A),
                                            modifier = Modifier.clickable { viewModel.clearEmotionHistory() }
                                        )
                                    }
                                }

                                if (emotionsList.isEmpty()) {
                                    Text(
                                        text = "No saved emotion chats yet. Connect with your chat buddy below!",
                                        fontSize = 12.sp,
                                        color = Color(0xFF3D3834).copy(alpha = 0.5f),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    emotionsList.forEach { emotion ->
                                        val isUser = emotion.sender == "USER"
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    start = if (isUser) 0.dp else 16.dp,
                                                    end = if (isUser) 16.dp else 0.dp
                                                )
                                                .clip(RoundedCornerShape(
                                                    topStart = 16.dp, 
                                                    topEnd = 16.dp, 
                                                    bottomStart = if (isUser) 16.dp else 4.dp, 
                                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                                ))
                                                .background(
                                                    if (isUser) Color.White.copy(alpha = 0.5f) 
                                                    else Color(0xFFE0A7A7).copy(alpha = 0.15f)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isUser) Color.Transparent else Color(0xFFE0A7A7).copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                                .padding(10.dp)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                val dateText = remember(emotion.timestamp) {
                                                    java.text.SimpleDateFormat("MMM dd, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(emotion.timestamp))
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = if (isUser) "You 👤" else "Lumee 🌸",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isUser) Color(0xFF3D3834) else Color(0xFF8B5A5A)
                                                    )
                                                    Text(
                                                        text = dateText,
                                                        fontSize = 9.sp,
                                                        color = Color(0xFF3D3834).copy(alpha = 0.4f)
                                                    )
                                                }
                                                Text(
                                                    text = emotion.text,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF3D3834)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { showProfileModal = false },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D3834)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Return to Sanctuary", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

fun shareQuoteAsImage(context: android.content.Context, quoteText: String, category: String) {
    try {
        // Create a bitmap
        val width = 800
        val height = 800
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val cv = android.graphics.Canvas(bitmap)

        // Draw a beautiful background. Let's make it a nice peach/pink pastel gradient card!
        val bgPaint = android.graphics.Paint()
        val gradient = android.graphics.LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            android.graphics.Color.parseColor("#FFF9E8"), // Peach gold
            android.graphics.Color.parseColor("#FBEBE6"), // Rose peach
            android.graphics.Shader.TileMode.CLAMP
        )
        bgPaint.shader = gradient
        cv.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Draw border or corner highlights
        val accentPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#E0A7A7")
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 12f
            isAntiAlias = true
        }
        cv.drawRoundRect(20f, 20f, width.toFloat() - 20f, height.toFloat() - 20f, 40f, 40f, accentPaint)

        // Draw decorative element
        val decorPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#E0A7A7")
            textSize = 60f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        cv.drawText("✦", width / 2f, 130f, decorPaint)

        // Category heading
        val categoryPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#8B5A5A")
            textSize = 30f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
        cv.drawText(category.uppercase(), width / 2f, 200f, categoryPaint)

        // Draw a nice separator line
        accentPaint.strokeWidth = 2f
        accentPaint.color = android.graphics.Color.parseColor("#E0A7A7")
        cv.drawLine(width / 2f - 80f, 240f, width / 2f + 80f, 240f, accentPaint)

        // Text Paint for the quote (wrapped)
        val textPaint = android.text.TextPaint().apply {
            color = android.graphics.Color.parseColor("#3D3834")
            textSize = 34f
            isAntiAlias = true
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
        }

        // Beautiful line-wrapping logic using StaticLayout or manual tokenizing
        val quoteWithQuotes = "“$quoteText”"
        val x = 85
        val yStart = 290f
        val textWidth = width - (x * 2)
        
        // Android StaticLayout is the ultimate text wrapper!
        val staticLayout = if (android.os.Build.VERSION.SDK_INT >= 23) {
            android.text.StaticLayout.Builder.obtain(quoteWithQuotes, 0, quoteWithQuotes.length, textPaint, textWidth)
                .setAlignment(android.text.Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(12f, 1f)
                .build()
        } else {
            @Suppress("DEPRECATION")
            android.text.StaticLayout(
                quoteWithQuotes, textPaint, textWidth,
                android.text.Layout.Alignment.ALIGN_CENTER, 1f, 12f, false
            )
        }

        cv.save()
        cv.translate(x.toFloat(), yStart)
        staticLayout.draw(cv)
        cv.restore()

        // Author name at the bottom
        val authorPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#8B5A5A")
            textSize = 30f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
        cv.drawText("— Lumee Sanctuary 🌸", width / 2f, height - 120f, authorPaint)

        // Save to file
        val shareDir = java.io.File(context.cacheDir, "shared_quotes")
        shareDir.mkdirs()
        val file = java.io.File(shareDir, "lumee_shared_quote.png")
        java.io.FileOutputStream(file).use { fos ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos)
        }

        // Share via Intent
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            putExtra(android.content.Intent.EXTRA_TEXT, "“$quoteText” — Shared from Lumee 🌸")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share beautiful quote"))
    } catch (e: Exception) {
        e.printStackTrace()
        android.widget.Toast.makeText(context, "Failed to create shareable image", android.widget.Toast.LENGTH_SHORT).show()
    }
}
