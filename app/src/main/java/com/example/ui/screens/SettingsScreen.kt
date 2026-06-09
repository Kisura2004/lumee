package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LumeeViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: LumeeViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val calendar = Calendar.getInstance()

    var nameInput by remember { mutableStateOf("") }
    var birthMonthInput by remember { mutableStateOf("") }
    var birthDayInput by remember { mutableStateOf("") }

    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderHour by remember { mutableStateOf("08") }
    var reminderMinute by remember { mutableStateOf("00") }

    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("lumee_prefs", android.content.Context.MODE_PRIVATE) }

    // Synchronize inputs with saved database values
    LaunchedEffect(userProfile) {
        userProfile?.let {
            nameInput = it.name
            birthMonthInput = it.birthMonth?.toString() ?: ""
            birthDayInput = it.birthDay?.toString() ?: ""

            val time = it.preferredReminderTime
            reminderEnabled = (time != null)
            if (time != null) {
                val parts = time.split(":")
                if (parts.size == 2) {
                    reminderHour = parts[0]
                    reminderMinute = parts[1]
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Screen Header
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 4.dp)
        ) {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF3D3834)
            )
            Text(
                text = "Make the companion uniquely yours",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF3D3834).copy(alpha = 0.5f)
            )
        }

        // Profile Details Card (Frosted Glass Container)
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
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE0A7A7).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Details",
                            tint = Color(0xFFE0A7A7),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Your Identity",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF3D3834)
                    )
                }

                // Name TextField
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = {
                        nameInput = it
                        viewModel.updateProfileName(it)
                    },
                    label = { Text("What should we call you?") },
                    placeholder = { Text("Friend") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_name_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.4f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.25f),
                        focusedBorderColor = Color(0xFFE0A7A7),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedTextColor = Color(0xFF3D3834),
                        unfocusedTextColor = Color(0xFF3D3834)
                    )
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.5f))

                // Birthday Inputs - Sincere birthday blessing triggers
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE0A7A7).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cake,
                            contentDescription = "Birthday trigger details",
                            tint = Color(0xFFE0A7A7),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Sincere Birthday Experience",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF3D3834)
                    )
                }

                Text(
                    text = "If provided, Lumee will offer a sincere, thoughtful morning greeting on your special day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF3D3834).copy(alpha = 0.6f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = birthMonthInput,
                        onValueChange = {
                            val filtered = it.filter { char -> char.isDigit() }
                            val month = filtered.toIntOrNull()
                            if (month == null || month in 1..12) {
                                birthMonthInput = filtered
                                viewModel.updateProfileBirthday(
                                    month = month,
                                    day = birthDayInput.toIntOrNull()
                                )
                            }
                        },
                        label = { Text("Month (1-12)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_birthday_month_input"),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.4f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.25f),
                            focusedBorderColor = Color(0xFFE0A7A7),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedTextColor = Color(0xFF3D3834),
                            unfocusedTextColor = Color(0xFF3D3834)
                        )
                    )

                    OutlinedTextField(
                        value = birthDayInput,
                        onValueChange = {
                            val filtered = it.filter { char -> char.isDigit() }
                            val day = filtered.toIntOrNull()
                            if (day == null || day in 1..31) {
                                birthDayInput = filtered
                                viewModel.updateProfileBirthday(
                                    month = birthMonthInput.toIntOrNull(),
                                    day = day
                                )
                            }
                        },
                        label = { Text("Day (1-31)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_birthday_day_input"),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.4f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.25f),
                            focusedBorderColor = Color(0xFFE0A7A7),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedTextColor = Color(0xFF3D3834),
                            unfocusedTextColor = Color(0xFF3D3834)
                        )
                    )
                }
            }
        }

        // Mindfulness Reminders Card (Frosted Glass Container)
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
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE0A7A7).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Mindfulness Reminders",
                                tint = Color(0xFFE0A7A7),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Mindfulness Reminders",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF3D3834)
                        )
                    }

                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { isChecked ->
                            reminderEnabled = isChecked
                            if (isChecked) {
                                val hr = reminderHour.ifEmpty { "08" }.padStart(2, '0')
                                val min = reminderMinute.ifEmpty { "00" }.padStart(2, '0')
                                viewModel.updateReminderTime("$hr:$min")
                            } else {
                                viewModel.updateReminderTime(null)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFE0A7A7),
                            uncheckedThumbColor = Color(0xFF3D3834).copy(alpha = 0.4f),
                            uncheckedTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                }

                Text(
                    text = "When active, Lumee will send a gentle local notification at your chosen time containing a slow reminder to focus on peace.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF3D3834).copy(alpha = 0.6f)
                )

                if (reminderEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = reminderHour,
                            onValueChange = { input ->
                                val filtered = input.filter { char -> char.isDigit() }
                                val hr = filtered.toIntOrNull()
                                if (hr == null || hr in 0..23) {
                                    reminderHour = filtered
                                    if (filtered.isNotEmpty()) {
                                        val mPart = reminderMinute.ifEmpty { "00" }.padStart(2, '0')
                                        val hPart = filtered.padStart(2, '0')
                                        viewModel.updateReminderTime("$hPart:$mPart")
                                    }
                                }
                            },
                            label = { Text("Hour (0-23)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_reminder_hour_input"),
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.4f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.25f),
                                focusedBorderColor = Color(0xFFE0A7A7),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = Color(0xFF3D3834),
                                unfocusedTextColor = Color(0xFF3D3834)
                            )
                        )

                        OutlinedTextField(
                            value = reminderMinute,
                            onValueChange = { input ->
                                val filtered = input.filter { char -> char.isDigit() }
                                val min = filtered.toIntOrNull()
                                if (min == null || min in 0..59) {
                                    reminderMinute = filtered
                                    if (filtered.isNotEmpty()) {
                                        val hPart = reminderHour.ifEmpty { "08" }.padStart(2, '0')
                                        val mPart = filtered.padStart(2, '0')
                                        viewModel.updateReminderTime("$hPart:$mPart")
                                    }
                                }
                            },
                            label = { Text("Minute (0-59)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_reminder_minute_input"),
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.4f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.25f),
                                focusedBorderColor = Color(0xFFE0A7A7),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = Color(0xFF3D3834),
                                unfocusedTextColor = Color(0xFF3D3834)
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Morning Wakeup & Evening Motivation Card
        var morningWakeupEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("morning_wakeup_enabled", true)) }
        var morningHour by remember {
            val fullTime = sharedPrefs.getString("morning_wakeup_time", "07:00") ?: "07:00"
            mutableStateOf(fullTime.split(":")[0])
        }
        var morningMinute by remember {
            val fullTime = sharedPrefs.getString("morning_wakeup_time", "07:00") ?: "07:00"
            mutableStateOf(fullTime.split(":")[1])
        }

        var eveningMotivationEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("evening_motivation_enabled", true)) }
        var eveningHour by remember {
            val fullTime = sharedPrefs.getString("evening_motivation_time", "21:30") ?: "21:30"
            mutableStateOf(fullTime.split(":")[0])
        }
        var eveningMinute by remember {
            val fullTime = sharedPrefs.getString("evening_motivation_time", "21:30") ?: "21:30"
            mutableStateOf(fullTime.split(":")[1])
        }

        fun saveMorningTime(hr: String, min: String) {
            val cleanHr = hr.ifEmpty { "07" }.padStart(2, '0')
            val cleanMin = min.ifEmpty { "00" }.padStart(2, '0')
            sharedPrefs.edit()
                .putString("morning_wakeup_time", "$cleanHr:$cleanMin")
                .apply()
            com.example.receiver.NotificationScheduler.scheduleNextNotification(context)
        }

        fun saveEveningTime(hr: String, min: String) {
            val cleanHr = hr.ifEmpty { "21" }.padStart(2, '0')
            val cleanMin = min.ifEmpty { "30" }.padStart(2, '0')
            sharedPrefs.edit()
                .putString("evening_motivation_time", "$cleanHr:$cleanMin")
                .apply()
            com.example.receiver.NotificationScheduler.scheduleNextNotification(context)
        }

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
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE0A7A7).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Daily Checkups",
                            tint = Color(0xFFE0A7A7),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Daily Wakeup & Night Checkups",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF3D3834)
                    )
                }

                Text(
                    text = "Configure specific early morning greetings and late-night motivation reflection cards.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF3D3834).copy(alpha = 0.6f)
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.4f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Morning Wakeup 🌅",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF3D3834)
                        )
                        Text(
                            text = "Early checkups with uplifting morning ideas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF3D3834).copy(alpha = 0.5f)
                        )
                    }

                    Switch(
                        checked = morningWakeupEnabled,
                        onCheckedChange = { isChecked ->
                            morningWakeupEnabled = isChecked
                            sharedPrefs.edit().putBoolean("morning_wakeup_enabled", isChecked).apply()
                            com.example.receiver.NotificationScheduler.scheduleNextNotification(context)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFE0A7A7),
                            uncheckedThumbColor = Color(0xFF3D3834).copy(alpha = 0.4f),
                            uncheckedTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                }

                if (morningWakeupEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = morningHour,
                            onValueChange = { input ->
                                val filtered = input.filter { char -> char.isDigit() }
                                val hr = filtered.toIntOrNull()
                                if (hr == null || hr in 0..23) {
                                    morningHour = filtered
                                    saveMorningTime(filtered, morningMinute)
                                }
                            },
                            label = { Text("Hour (0-23)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.4f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.25f),
                                focusedBorderColor = Color(0xFFE0A7A7),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = Color(0xFF3D3834),
                                unfocusedTextColor = Color(0xFF3D3834)
                            )
                        )

                        OutlinedTextField(
                            value = morningMinute,
                            onValueChange = { input ->
                                val filtered = input.filter { char -> char.isDigit() }
                                val min = filtered.toIntOrNull()
                                if (min == null || min in 0..59) {
                                    morningMinute = filtered
                                    saveMorningTime(morningHour, filtered)
                                }
                            },
                            label = { Text("Minute (0-59)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.4f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.25f),
                                focusedBorderColor = Color(0xFFE0A7A7),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = Color(0xFF3D3834),
                                unfocusedTextColor = Color(0xFF3D3834)
                            )
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.4f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Evening Motivation 🌌",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF3D3834)
                        )
                        Text(
                            text = "Late night motivation and soothing quotes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF3D3834).copy(alpha = 0.5f)
                        )
                    }

                    Switch(
                        checked = eveningMotivationEnabled,
                        onCheckedChange = { isChecked ->
                            eveningMotivationEnabled = isChecked
                            sharedPrefs.edit().putBoolean("evening_motivation_enabled", isChecked).apply()
                            com.example.receiver.NotificationScheduler.scheduleNextNotification(context)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFE0A7A7),
                            uncheckedThumbColor = Color(0xFF3D3834).copy(alpha = 0.4f),
                            uncheckedTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                }

                if (eveningMotivationEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = eveningHour,
                            onValueChange = { input ->
                                val filtered = input.filter { char -> char.isDigit() }
                                val hr = filtered.toIntOrNull()
                                if (hr == null || hr in 0..23) {
                                    eveningHour = filtered
                                    saveEveningTime(filtered, eveningMinute)
                                }
                            },
                            label = { Text("Hour (0-23)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.4f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.25f),
                                focusedBorderColor = Color(0xFFE0A7A7),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = Color(0xFF3D3834),
                                unfocusedTextColor = Color(0xFF3D3834)
                            )
                        )

                        OutlinedTextField(
                            value = eveningMinute,
                            onValueChange = { input ->
                                val filtered = input.filter { char -> char.isDigit() }
                                val min = filtered.toIntOrNull()
                                if (min == null || min in 0..59) {
                                    eveningMinute = filtered
                                    saveEveningTime(eveningHour, filtered)
                                }
                            },
                            label = { Text("Minute (0-59)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.4f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.25f),
                                focusedBorderColor = Color(0xFFE0A7A7),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = Color(0xFF3D3834),
                                unfocusedTextColor = Color(0xFF3D3834)
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Seasonal Awareness Visualizer Card
        val currentSeasonName = when (calendar.get(Calendar.MONTH)) {
            Calendar.DECEMBER, Calendar.JANUARY, Calendar.FEBRUARY -> "Cozy Autumn-Winter"
            Calendar.MARCH, Calendar.APRIL, Calendar.MAY -> "Spring Blossom"
            Calendar.JUNE, Calendar.JULY, Calendar.AUGUST -> "Abundant Summer Light"
            else -> "Golden Harvest Autumn"
        }
        val currentSeasonEmoji = when (calendar.get(Calendar.MONTH)) {
            Calendar.DECEMBER, Calendar.JANUARY, Calendar.FEBRUARY -> "❄️"
            Calendar.MARCH, Calendar.APRIL, Calendar.MAY -> "🌱"
            Calendar.JUNE, Calendar.JULY, Calendar.AUGUST -> "☀️"
            else -> "🍂"
        }

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
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE0A7A7).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = currentSeasonEmoji, fontSize = 20.sp)
                }

                Column {
                    Text(
                        text = "Season Awareness: $currentSeasonName",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF3D3834)
                    )
                    Text(
                        text = "Lumee senses the environment. Your greetings are subtly guided by changes in the seasons of the year.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF3D3834).copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Privacy First Pillar Card
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
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE0A7A7).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PrivacyTip,
                            contentDescription = "Privacy Shield",
                            tint = Color(0xFFE0A7A7),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Device Sanctuary Policy",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF3D3834)
                    )
                }

                Text(
                    text = "Reflections, names, and birthdays stay safely stored on this device in an offline-only SQLite database. Lumee never uploads your raw thoughts, keeping your mental harbor private.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF3D3834).copy(alpha = 0.6f)
                )
            }
        }

        // System description of quiet companion
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Lumee Companion • Version 1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF3D3834).copy(alpha = 0.4f)
            )
            Text(
                text = "A gentle beacon of light. Unconnected, offline, peaceful.",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF3D3834).copy(alpha = 0.3f),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
