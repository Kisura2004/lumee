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
import com.example.ui.LumeeViewModel
import java.util.Calendar

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
