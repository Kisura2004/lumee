package com.example.data

import com.example.data.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class LumeeRepository(private val dao: LumeeDao) {

    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfileFlow()
    val moments: Flow<List<MomentEntity>> = dao.getAllMomentsFlow()
    val greetingHistory: Flow<List<GreetingHistoryEntity>> = dao.getGreetingHistoryFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    suspend fun getOrCreateProfile(): UserProfileEntity = withContext(Dispatchers.IO) {
        var profile = dao.getUserProfile()
        if (profile == null) {
            profile = UserProfileEntity()
            dao.insertUserProfile(profile)
        }
        profile
    }

    suspend fun updateProfileName(name: String) = withContext(Dispatchers.IO) {
        val profile = getOrCreateProfile()
        dao.insertUserProfile(profile.copy(name = name))
    }

    suspend fun updateProfileBirthday(month: Int?, day: Int?) = withContext(Dispatchers.IO) {
        val profile = getOrCreateProfile()
        dao.insertUserProfile(profile.copy(birthMonth = month, birthDay = day))
    }

    suspend fun updateReminderTime(time: String?) = withContext(Dispatchers.IO) {
        val profile = getOrCreateProfile()
        dao.insertUserProfile(profile.copy(preferredReminderTime = time))
    }

    /**
     * Resolves today's active greeting and processes streak updates.
     * Returns a Pair: (greetingText, categoryName)
     */
    suspend fun resolveTodayGreeting(forceRefresh: Boolean = false): Pair<String, String> = withContext(Dispatchers.IO) {
        val profile = getOrCreateProfile()
        val calendar = Calendar.getInstance()
        val todayStr = dateFormat.format(calendar.time)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY) // 0-23
        val currentMonth = calendar.get(Calendar.MONTH) + 1  // 1-12
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH) // 1-31

        // Check is it a birthday?
        val isBirthday = profile.birthMonth == currentMonth && profile.birthDay == currentDay

        if (isBirthday) {
            // Sincere birthday greeting of Lumee
            val bdayGreetings = GreetingProvider.birthdayGreetings
            val completedTodayProfile = profile.copy(
                lastGreetingDate = todayStr,
                completedToday = true
            )
            dao.insertUserProfile(completedTodayProfile)
            return@withContext Pair(
                bdayGreetings.first().replace("Friend", profile.name),
                "Birthday Blessing"
            )
        }

        // Determine category / time of day header
        val timeHeader = fun(catName: String): String {
            return when (currentHour) {
                in 5..11 -> "Morning $catName"
                in 12..16 -> "Afternoon $catName"
                else -> "Evening $catName"
            }
        }

        // Check if there is already a saved greeting for today in DB and they haven't forced a refresh
        if (!forceRefresh && profile.lastGreetingDate == todayStr && profile.lastGreetingId != null) {
            val savedIdx = if (profile.lastGreetingId >= 10000) {
                (profile.lastGreetingId - 10000).coerceIn(0, GreetingProvider.TOTAL_GENERATIVE_QUOTES - 1)
            } else {
                Math.abs(profile.lastGreetingId) % GreetingProvider.TOTAL_GENERATIVE_QUOTES
            }
            val savedGreeting = GreetingProvider.getQuoteAt(savedIdx)
            val header = timeHeader(savedGreeting.category.name.replace("_", " ").lowercase().replaceFirstChar { it.titlecase() })
            return@withContext Pair(savedGreeting.text, header)
        }

        // Otherwise (it's a new day or they clicked forceRefresh)
        // Select a fresh quote from the massive 40,000 pool!
        // To make daily quotes consistent unless forced, we deterministic-hash the day's string if not forced.
        val quoteIndex = if (forceRefresh) {
            // Truly random but avoid same if possible
            val currentIdx = if (profile.lastGreetingId != null) {
                if (profile.lastGreetingId >= 10000) profile.lastGreetingId - 10000 else Math.abs(profile.lastGreetingId)
            } else -1
            var randIdx = (0 until GreetingProvider.TOTAL_GENERATIVE_QUOTES).random()
            // Try to find a different index
            var retries = 5
            while (randIdx == currentIdx && retries > 0) {
                randIdx = (0 until GreetingProvider.TOTAL_GENERATIVE_QUOTES).random()
                retries--
            }
            randIdx
        } else {
            // Deterministic hash based on today's date string
            Math.abs(todayStr.hashCode()) % GreetingProvider.TOTAL_GENERATIVE_QUOTES
        }

        val selected = GreetingProvider.getQuoteAt(quoteIndex)

        // Streak computation:
        // Did they check in yesterday?
        var newStreak = profile.streakCount
        if (profile.lastGreetingDate != todayStr) {
            val yesterdayCalendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            val yesterdayStr = dateFormat.format(yesterdayCalendar.time)

            if (profile.lastGreetingDate == yesterdayStr) {
                // Checked in yesterday, grow streak!
                newStreak += 1
            } else {
                // First time of check-in, or missed yesterday, start over gently
                newStreak = 1
            }
        }

        // Save log
        dao.insertGreetingHistory(
            GreetingHistoryEntity(
                greetingId = selected.id,
                dateString = todayStr
            )
        )

        // Save updated profile
        dao.insertUserProfile(
            profile.copy(
                lastGreetingDate = todayStr,
                lastGreetingId = selected.id,
                completedToday = true,
                streakCount = newStreak
            )
        )

        val header = timeHeader(selected.category.name.replace("_", " ").lowercase().replaceFirstChar { it.titlecase() })
        Pair(selected.text, header)
    }

    suspend fun saveMoment(
        content: String,
        prompt: String? = null,
        imageUri: String? = null,
        videoUri: String? = null,
        audioUri: String? = null
    ) = withContext(Dispatchers.IO) {
        if (content.trim().isNotEmpty() || imageUri != null || videoUri != null || audioUri != null) {
            dao.insertMoment(
                MomentEntity(
                    content = content.trim(),
                    timestamp = System.currentTimeMillis(),
                    prompt = prompt,
                    imageUri = imageUri,
                    videoUri = videoUri,
                    audioUri = audioUri
                )
            )
        }
    }

    suspend fun deleteMoment(moment: MomentEntity) = withContext(Dispatchers.IO) {
        dao.deleteMoment(moment)
    }

    suspend fun updateProfilePassword(password: String?) = withContext(Dispatchers.IO) {
        val profile = getOrCreateProfile()
        dao.insertUserProfile(profile.copy(password = password))
    }

    // Emotion Helpers
    val emotions: Flow<List<com.example.data.database.EmotionEntity>> = dao.getAllEmotionsFlow()

    suspend fun insertEmotion(sender: String, text: String, emotionType: String? = null) = withContext(Dispatchers.IO) {
        dao.insertEmotion(
            com.example.data.database.EmotionEntity(
                sender = sender,
                text = text.trim(),
                emotionType = emotionType
            )
        )
    }

    suspend fun clearEmotions() = withContext(Dispatchers.IO) {
        dao.clearEmotions()
    }

    suspend fun clearOldLogData() = withContext(Dispatchers.IO) {
        // Keep DB lightweight
    }
}
