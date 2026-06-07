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
import androidx.compose.material3.*
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
            // Left Date cluster
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
                        text = if (streak > 0) "✨" else "🌱",
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

        Spacer(modifier = Modifier.height(24.dp))
    }
}
