package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "greeting_history")
data class GreetingHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val greetingId: Int,
    val dateString: String, // yyyy-MM-dd
    val timestamp: Long = System.currentTimeMillis()
)
