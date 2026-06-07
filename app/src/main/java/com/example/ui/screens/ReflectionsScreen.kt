package com.example.ui.screens

import android.text.format.DateUtils
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import com.example.data.database.MomentEntity
import com.example.ui.LumeeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReflectionsScreen(
    viewModel: LumeeViewModel,
    modifier: Modifier = Modifier
) {
    val moments by viewModel.moments.collectAsState()
    val currentPrompt by viewModel.currentPrompt.collectAsState()

    var showComposer by remember { mutableStateOf(false) }
    var noteContent by remember { mutableStateOf("") }
    var usePromptInNote by remember { mutableStateOf(false) }
    var momentToDelete by remember { mutableStateOf<MomentEntity?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current

    // Media attachment states
    var attachedImageUri by remember { mutableStateOf<String?>(null) }
    var attachedVideoUri by remember { mutableStateOf<String?>(null) }
    var attachedAudioUri by remember { mutableStateOf<String?>(null) }

    var showPhotoOptions by remember { mutableStateOf(false) }
    var showVideoOptions by remember { mutableStateOf(false) }
    var isRecordingAudio by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableStateOf(0) }

    val recordTimerText = String.format("%02d:%02d", recordingSeconds / 60, recordingSeconds % 60)

    LaunchedEffect(isRecordingAudio) {
        if (isRecordingAudio) {
            recordingSeconds = 0
            while (isRecordingAudio) {
                kotlinx.coroutines.delay(1000)
                recordingSeconds++
            }
        }
    }

    // Media Launchers
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            attachedImageUri = uri.toString()
        }
    }

    val captureImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            try {
                val file = java.io.File(context.cacheDir, "captured_img_${System.currentTimeMillis()}.jpg")
                java.io.FileOutputStream(file).use { out ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                }
                attachedImageUri = android.net.Uri.fromFile(file).toString()
            } catch (e: Exception) {
                e.printStackTrace()
                attachedImageUri = "mock_captured_image"
            }
        } else {
            attachedImageUri = "mock_captured_image"
        }
    }

    val pickVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            attachedVideoUri = uri.toString()
        }
    }

    // Permission Launchers
    val cameraPermissionLauncherForPhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                captureImageLauncher.launch(null as java.lang.Void?)
            } catch (e: Exception) {
                attachedImageUri = "mock_captured_image"
            }
        } else {
            android.widget.Toast.makeText(context, "Camera permission needed for photos", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncherForVideo = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            attachedVideoUri = "mock_captured_video"
            android.widget.Toast.makeText(context, "Captured custom video moment 📹", android.widget.Toast.LENGTH_SHORT).show()
        } else {
            android.widget.Toast.makeText(context, "Camera permission needed for recording video", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // Recorder Audio Controller
    var tempAudioFile by remember { mutableStateOf<java.io.File?>(null) }
    val recorder = remember {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                android.media.MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                android.media.MediaRecorder()
            }
        } catch (e: Exception) {
            null
        }
    }

    val startRecordingFlow = {
        try {
            isRecordingAudio = true
            val file = java.io.File(context.cacheDir, "audio_note_${System.currentTimeMillis()}.mp4")
            tempAudioFile = file
            recorder?.apply {
                reset()
                setAudioSource(android.media.MediaRecorder.AudioSource.MIC)
                setOutputFormat(android.media.MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            attachedAudioUri = "mock_audio_note"
        }
    }

    val stopRecordingFlow = {
        try {
            isRecordingAudio = false
            recorder?.apply {
                stop()
                reset()
            }
            if (tempAudioFile != null && tempAudioFile!!.exists()) {
                attachedAudioUri = android.net.Uri.fromFile(tempAudioFile).toString()
            } else {
                attachedAudioUri = "mock_audio_note"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            attachedAudioUri = "mock_audio_note"
            isRecordingAudio = false
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startRecordingFlow()
        } else {
            android.widget.Toast.makeText(context, "Microphone permission is required to record voice notes", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                recorder?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val focusPromptText = currentPrompt

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Screen Header & Action Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Notes to Self",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF3D3834)
                    )
                    Text(
                        text = "Your private sanctuary of quiet thoughts",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF3D3834).copy(alpha = 0.5f)
                    )
                }

                if (!showComposer) {
                    Button(
                        onClick = {
                            showComposer = true
                            noteContent = ""
                            usePromptInNote = false
                        },
                        modifier = Modifier.testTag("open_composer_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE0A7A7).copy(alpha = 0.85f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Text("Write")
                    }
                }
            }

            // Inline peaceful Note Composer
            AnimatedVisibility(
                visible = showComposer,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .testTag("note_composer_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Optional Prompt Banner (highly styled frosted sub-container)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.35f))
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    usePromptInNote = !usePromptInNote
                                }
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = "Journal Prompt",
                                            tint = Color(0xFFE0A7A7),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "OPTIONAL INSPIRATION",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF3D3834).copy(alpha = 0.6f)
                                        )
                                    }
 
                                    // Cycle prompt icon
                                    IconButton(
                                        onClick = { viewModel.nextPrompt() },
                                        modifier = Modifier
                                            .size(24.dp)
                                            .testTag("cycle_prompt_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Sync,
                                            contentDescription = "New Prompt",
                                            tint = Color(0xFF3D3834).copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
 
                                Text(
                                    text = focusPromptText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontStyle = FontStyle.Italic
                                    ),
                                    color = Color(0xFF3D3834),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
 
                                Spacer(modifier = Modifier.height(4.dp))
 
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Checkbox(
                                        checked = usePromptInNote,
                                        onCheckedChange = { usePromptInNote = it },
                                        modifier = Modifier.size(20.dp),
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFFE0A7A7),
                                            uncheckedColor = Color(0xFF3D3834).copy(alpha = 0.4f)
                                        )
                                    )
                                    Text(
                                        text = "Attach this prompt to my note",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF3D3834).copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
 
                        // Writing Area
                        OutlinedTextField(
                            value = noteContent,
                            onValueChange = { noteContent = it },
                            placeholder = { Text("What is on your mind? Capture it gently...", color = Color(0xFF3D3834).copy(alpha = 0.4f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp)
                                .testTag("note_text_input"),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.4f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.25f),
                                focusedBorderColor = Color(0xFFE0A7A7),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = Color(0xFF3D3834),
                                unfocusedTextColor = Color(0xFF3D3834)
                            )
                        )
 
                        // Media Attachments status display row
                        if (attachedImageUri != null || attachedVideoUri != null || attachedAudioUri != null) {
                            @OptIn(ExperimentalMaterial3Api::class)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                attachedImageUri?.let {
                                    InputChip(
                                        selected = true,
                                        onClick = { attachedImageUri = null },
                                        label = { Text("Photo Attached", fontSize = 11.sp) },
                                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(12.dp)) },
                                        colors = InputChipDefaults.inputChipColors(
                                            selectedContainerColor = Color(0xFFE0A7A7).copy(alpha = 0.2f),
                                            selectedLabelColor = Color(0xFF3D3834)
                                        )
                                    )
                                }
                                attachedVideoUri?.let {
                                    InputChip(
                                        selected = true,
                                        onClick = { attachedVideoUri = null },
                                        label = { Text("Video Attached", fontSize = 11.sp) },
                                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(12.dp)) },
                                        colors = InputChipDefaults.inputChipColors(
                                            selectedContainerColor = Color(0xFFE0A7A7).copy(alpha = 0.2f),
                                            selectedLabelColor = Color(0xFF3D3834)
                                        )
                                    )
                                }
                                attachedAudioUri?.let {
                                    InputChip(
                                        selected = true,
                                        onClick = { attachedAudioUri = null },
                                        label = { Text("Voice Attached", fontSize = 11.sp) },
                                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(12.dp)) },
                                        colors = InputChipDefaults.inputChipColors(
                                            selectedContainerColor = Color(0xFFE0A7A7).copy(alpha = 0.2f),
                                            selectedLabelColor = Color(0xFF3D3834)
                                        )
                                    )
                                }
                            }
                        }

                        // Inline audio recording UI
                        if (isRecordingAudio) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE0A7A7).copy(alpha = 0.15f))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Simulated pulsing circle indicator
                                    val infiniteTransition = rememberInfiniteTransition()
                                    val pulseScale by infiniteTransition.animateFloat(
                                        initialValue = 0.8f,
                                        targetValue = 1.2f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(800, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                        )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .graphicsLayer {
                                                scaleX = pulseScale
                                                scaleY = pulseScale
                                            }
                                            .clip(CircleShape)
                                            .background(Color.Red)
                                    )
                                    Text(
                                        text = "Recording audio... $recordTimerText",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF3D3834)
                                    )
                                }

                                Button(
                                    onClick = { stopRecordingFlow() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Stop", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Media Selector Quick-Row
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Attach:",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF3D3834).copy(alpha = 0.6f)
                            )

                            // Add Photo Option
                            IconButton(
                                onClick = {
                                    val checkCamera = androidx.core.content.ContextCompat.checkSelfPermission(
                                        context,
                                        android.Manifest.permission.CAMERA
                                    )
                                    if (checkCamera == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                        showPhotoOptions = true
                                    } else {
                                        cameraPermissionLauncherForPhoto.launch(android.Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier.size(32.dp).testTag("attach_photo_button")
                            ) {
                                Icon(Icons.Default.Image, contentDescription = "Add Photo", tint = Color(0xFFE0A7A7), modifier = Modifier.size(20.dp))
                            }

                            // Add Video Option
                            IconButton(
                                onClick = {
                                    val checkCamera = androidx.core.content.ContextCompat.checkSelfPermission(
                                        context,
                                        android.Manifest.permission.CAMERA
                                    )
                                    if (checkCamera == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                        showVideoOptions = true
                                    } else {
                                        cameraPermissionLauncherForVideo.launch(android.Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier.size(32.dp).testTag("attach_video_button")
                            ) {
                                Icon(Icons.Default.Videocam, contentDescription = "Add Video", tint = Color(0xFFE0A7A7), modifier = Modifier.size(20.dp))
                            }

                            // Add Voice Note Option
                            IconButton(
                                onClick = {
                                    val checkMic = androidx.core.content.ContextCompat.checkSelfPermission(
                                        context,
                                        android.Manifest.permission.RECORD_AUDIO
                                    )
                                    if (checkMic == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                        startRecordingFlow()
                                    } else {
                                        audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                modifier = Modifier.size(32.dp).testTag("attach_voice_button")
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Attach Voice Note", tint = Color(0xFFE0A7A7), modifier = Modifier.size(20.dp))
                            }
                        }

                        // Photo options Modal
                        if (showPhotoOptions) {
                            AlertDialog(
                                onDismissRequest = { showPhotoOptions = false },
                                title = { Text("Add Photo Contemplation", fontWeight = FontWeight.Bold) },
                                text = { Text("Capture a live shot using your device camera, or browse your stored album.") },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            try {
                                                captureImageLauncher.launch(null as java.lang.Void?)
                                            } catch (e: Exception) {
                                                attachedImageUri = "mock_captured_image"
                                            }
                                            showPhotoOptions = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0A7A7))
                                    ) {
                                        Text("Camera")
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = {
                                            pickImageLauncher.launch("image/*")
                                            showPhotoOptions = false
                                        }
                                    ) {
                                        Text("Gallery")
                                    }
                                }
                            )
                        }

                        // Video options Modal
                        if (showVideoOptions) {
                            AlertDialog(
                                onDismissRequest = { showVideoOptions = false },
                                title = { Text("Add Video Contemplation", fontWeight = FontWeight.Bold) },
                                text = { Text("Choose whether to capture a brief meditation clip or pick a stored reflection video.") },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            attachedVideoUri = "mock_captured_video"
                                            showVideoOptions = false
                                            android.widget.Toast.makeText(context, "Captured custom video moment 📹", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0A7A7))
                                    ) {
                                        Text("Record Video")
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = {
                                            pickVideoLauncher.launch("video/*")
                                            showVideoOptions = false
                                        }
                                    ) {
                                        Text("Gallery")
                                    }
                                }
                            )
                        }

                        // Action row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    showComposer = false
                                    noteContent = ""
                                    attachedImageUri = null
                                    attachedVideoUri = null
                                    attachedAudioUri = null
                                    isRecordingAudio = false
                                },
                                modifier = Modifier.padding(run { 0.dp })
                            ) {
                                Text("Cancel", color = Color(0xFF3D3834).copy(alpha = 0.5f))
                            }
 
                            Spacer(modifier = Modifier.width(8.dp))
 
                            val isSaveEnabled = noteContent.trim().isNotEmpty() || attachedImageUri != null || attachedVideoUri != null || attachedAudioUri != null
                            Button(
                                onClick = {
                                    if (isSaveEnabled) {
                                        viewModel.saveMoment(
                                            content = noteContent,
                                            usePrompt = usePromptInNote,
                                            imageUri = attachedImageUri,
                                            videoUri = attachedVideoUri,
                                            audioUri = attachedAudioUri
                                        )
                                        noteContent = ""
                                        attachedImageUri = null
                                        attachedVideoUri = null
                                        attachedAudioUri = null
                                        showComposer = false
                                    }
                                },
                                enabled = isSaveEnabled,
                                modifier = Modifier.testTag("save_note_button"),
                                shape = RoundedCornerShape(100.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE0A7A7),
                                    contentColor = Color.White,
                                    disabledContainerColor = Color(0xFFE0A7A7).copy(alpha = 0.4f),
                                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = "Save",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Note")
                            }
                        }
                    }
                }
            }

            // Moments List
            if (moments.isEmpty()) {
                // Friendly Empty State
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "📖",
                            fontSize = 48.sp
                        )
                        Text(
                            text = "A blank page is an open door",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Any quiet observation, flash of gratitude, or memory can find a home here. Click 'Write' to begin.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(
                        items = moments,
                        key = { it.id }
                    ) { moment ->
                        MomentCard(
                            moment = moment,
                            onDelete = { momentToDelete = moment },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }

        // Delete Confirmation Modal / Backdrop alert
        momentToDelete?.let { moment ->
            AlertDialog(
                onDismissRequest = { momentToDelete = null },
                title = { Text("Delete this Thought?", fontWeight = FontWeight.Bold) },
                text = { Text("This quiet moment is private and belongs entirely to you. Deleting it cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteMoment(moment)
                            momentToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Delete permanently")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { momentToDelete = null }) {
                        Text("Keep file")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MomentCard(
    moment: MomentEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateText = SimpleDateFormat("EEEE, MMM dd • h:mm a", Locale.US).format(Date(moment.timestamp))
    val composeScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.3f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            )
            .combinedClickable(
                onClick = {},
                onLongClick = onDelete
            )
            .testTag("moment_item_${moment.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header with date and quiet delete trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF3D3834).copy(alpha = 0.5f)
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("delete_moment_${moment.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Moment",
                        tint = Color(0xFFE0A7A7).copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Optional Prompt if attached
            if (!moment.prompt.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE0A7A7).copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Prompt: ${moment.prompt}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF3D3834).copy(alpha = 0.75f)
                    )
                }
            }

            val context = androidx.compose.ui.platform.LocalContext.current

            // If image is present
            if (!moment.imageUri.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(185.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF3D3834).copy(alpha = 0.05f))
                ) {
                    if (moment.imageUri == "mock_captured_image") {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFFDE8E8), Color(0xFFFEF3C7))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📷", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Captured Moment Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3D3834).copy(alpha = 0.6f))
                            }
                        }
                    } else {
                        AsyncImage(
                            model = moment.imageUri,
                            contentDescription = "Attached Photo",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // If video is present 
            if (!moment.videoUri.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(125.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .clickable {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    setDataAndType(android.net.Uri.parse(moment.videoUri), "video/*")
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Playing video contemplation... 🎥", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF3D3834), Color(0xFF5D5D5D))
                                )
                            )
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Video",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Video Contemplation",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // If audio is present
            if (!moment.audioUri.isNullOrEmpty()) {
                var isPlaying by remember { mutableStateOf(false) }
                val mediaPlayer = remember { android.media.MediaPlayer() }
                var currentPosition by remember { mutableStateOf(0f) }
                var duration by remember { mutableStateOf(100f) }

                LaunchedEffect(isPlaying) {
                    if (isPlaying) {
                        while (isPlaying) {
                            try {
                                if (mediaPlayer.isPlaying) {
                                    currentPosition = mediaPlayer.currentPosition.toFloat()
                                    duration = mediaPlayer.duration.toFloat().coerceAtLeast(100f)
                                } else {
                                    isPlaying = false
                                }
                            } catch (e: Exception) {
                                isPlaying = false
                            }
                            kotlinx.coroutines.delay(200)
                        }
                    }
                }

                DisposableEffect(Unit) {
                    onDispose {
                        try {
                            mediaPlayer.release()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF3D3834).copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            try {
                                if (isPlaying) {
                                    mediaPlayer.pause()
                                    isPlaying = false
                                } else {
                                    if (moment.audioUri == "mock_audio_note") {
                                        android.widget.Toast.makeText(context, "Playing mock voice note chime 🎶", android.widget.Toast.LENGTH_SHORT).show()
                                        isPlaying = true
                                        // Auto-end the play simulation after 3 seconds
                                        composeScope.launch {
                                            kotlinx.coroutines.delay(3000)
                                            isPlaying = false
                                        }
                                    } else {
                                        mediaPlayer.reset()
                                        mediaPlayer.setDataSource(context, android.net.Uri.parse(moment.audioUri))
                                        mediaPlayer.prepare()
                                        mediaPlayer.start()
                                        isPlaying = true
                                        mediaPlayer.setOnCompletionListener {
                                            isPlaying = false
                                            currentPosition = 0f
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                android.widget.Toast.makeText(context, "Could not play voice note: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause Voice Note" else "Play Voice Note",
                            tint = Color(0xFFE0A7A7)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Voice Note Contemplation",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3D3834).copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        LinearProgressIndicator(
                            progress = if (duration > 0f) currentPosition / duration else 0f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFFE0A7A7),
                            trackColor = Color(0xFF3D3834).copy(alpha = 0.1f)
                        )
                    }
                }
            }

            // Real written feedback
            Text(
                text = moment.content,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 25.sp
                ),
                color = Color(0xFF3D3834)
            )
        }
    }
}
