package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LumeeDao {
    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    // Moments (Reflections)
    @Query("SELECT * FROM moments ORDER BY timestamp DESC")
    fun getAllMomentsFlow(): Flow<List<MomentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoment(moment: MomentEntity)

    @Delete
    suspend fun deleteMoment(moment: MomentEntity)

    @Query("DELETE FROM moments WHERE id = :id")
    suspend fun deleteMomentById(id: Int)

    // Greeting History
    @Query("SELECT * FROM greeting_history ORDER BY timestamp DESC")
    fun getGreetingHistoryFlow(): Flow<List<GreetingHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGreetingHistory(history: GreetingHistoryEntity)

    @Query("SELECT greetingId FROM greeting_history WHERE timestamp > :sinceTimestamp")
    suspend fun getRecentGreetingIds(sinceTimestamp: Long): List<Int>

    // Local Emotions Chat Buddy Logs
    @Query("SELECT * FROM emotions ORDER BY timestamp ASC")
    fun getAllEmotionsFlow(): Flow<List<EmotionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmotion(emotion: EmotionEntity)

    @Query("DELETE FROM emotions")
    suspend fun clearEmotions()
}
