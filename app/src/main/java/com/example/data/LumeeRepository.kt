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

        // Rule: After 11:00 AM, full greetings are never shown.
        // Tone shifts to shorter supportive messages.
        if (currentHour >= 11) {
            val text = if (forceRefresh) {
                GreetingProvider.afternoonGreetings.random()
            } else {
                val index = Math.abs(todayStr.hashCode()) % GreetingProvider.afternoonGreetings.size
                GreetingProvider.afternoonGreetings[index]
            }
            
            // Mark complete without growing morning streak (they missed morning window)
            // But don't break the streak immediately inside database, let the next morning resolve it!
            if (profile.lastGreetingDate != todayStr || forceRefresh) {
                dao.insertUserProfile(profile.copy(
                    lastGreetingDate = todayStr,
                    lastGreetingId = -999,
                    completedToday = false
                ))
            }
            return@withContext Pair(text, "Supportive Note")
        }

        // Before 11:00 AM: We show morning greeting
        // If they already checked in today, load today's saved greeting
        if (!forceRefresh && profile.lastGreetingDate == todayStr && profile.lastGreetingId != null) {
            val savedGreeting = GreetingProvider.morningGreetings.find { it.id == profile.lastGreetingId }
                ?: GreetingProvider.getSeasonalGreeting(calendar).find { it.id == profile.lastGreetingId }
            return@withContext Pair(
                savedGreeting?.text ?: "Good morning. A beautiful day awaits.",
                savedGreeting?.category?.name ?: "Greeting of the Day"
            )
        }

        // Select a fresh greeting
        // Repetition Prevention: Exclude last 7 shown days greetings
        val recentIdsRaw = dao.getRecentGreetingIds(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L).toSet()
        val recentIds = if (forceRefresh && profile.lastGreetingId != null) {
            recentIdsRaw + profile.lastGreetingId
        } else {
            recentIdsRaw
        }

        // Combine standard and seasonal greetings to choose from
        val seasonGreetings = GreetingProvider.getSeasonalGreeting(calendar)
        val candidates = (GreetingProvider.morningGreetings + seasonGreetings)
            .filter { it.id !in recentIds }
            .ifEmpty { 
                if (forceRefresh && profile.lastGreetingId != null) {
                    (GreetingProvider.morningGreetings + seasonGreetings).filter { it.id != profile.lastGreetingId }
                } else {
                    GreetingProvider.morningGreetings + seasonGreetings
                }
            }

        // Choose one greeting
        val selected = candidates.randomOrNull() ?: GreetingProvider.morningGreetings.first()

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

        Pair(selected.text, selected.category.name.replace("_", " "))
    }

    suspend fun saveMoment(content: String, prompt: String? = null) = withContext(Dispatchers.IO) {
        if (content.trim().isNotEmpty()) {
            dao.insertMoment(
                MomentEntity(
                    content = content.trim(),
                    timestamp = System.currentTimeMillis(),
                    prompt = prompt
                )
            )
        }
    }

    suspend fun deleteMoment(moment: MomentEntity) = withContext(Dispatchers.IO) {
        dao.deleteMoment(moment)
    }

    suspend fun clearOldLogData() = withContext(Dispatchers.IO) {
        // Keep DB lightweight
    }
}
