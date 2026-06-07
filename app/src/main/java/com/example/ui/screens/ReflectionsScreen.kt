package com.example.ui.screens

import android.text.format.DateUtils
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                                },
                                modifier = Modifier.padding(run { 0.dp })
                            ) {
                                Text("Cancel", color = Color(0xFF3D3834).copy(alpha = 0.5f))
                            }
 
                            Spacer(modifier = Modifier.width(8.dp))
 
                            Button(
                                onClick = {
                                    if (noteContent.trim().isNotEmpty()) {
                                        viewModel.saveMoment(noteContent, usePromptInNote)
                                        noteContent = ""
                                        showComposer = false
                                    }
                                },
                                enabled = noteContent.trim().isNotEmpty(),
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
