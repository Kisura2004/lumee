package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Friend",
    val birthMonth: Int? = null, // 1-12
    val birthDay: Int? = null,   // 1-31
    val streakCount: Int = 0,
    val lastGreetingDate: String? = null, // yyyy-MM-dd
    val lastGreetingId: Int? = null,
    val completedToday: Boolean = false,
    val preferredReminderTime: String? = "08:00"
)
