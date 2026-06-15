package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emotions")
data class EmotionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "USER" or "BUDDY"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val emotionType: String? = null
)
