package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LumeeRepository
import com.example.data.database.AppDatabase
import com.example.data.database.MomentEntity
import com.example.data.database.UserProfileEntity
import com.example.receiver.NotificationScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class LumeeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LumeeRepository
    
    // UI states
    val userProfile: StateFlow<UserProfileEntity?>
    val moments: StateFlow<List<MomentEntity>>
    val emotions: StateFlow<List<com.example.data.database.EmotionEntity>>
    
    private val _todayGreeting = MutableStateFlow<Pair<String, String>?>(null)
    val todayGreeting: StateFlow<Pair<String, String>?> = _todayGreeting.asStateFlow()

    private val _isGreetingLoading = MutableStateFlow(false)
    val isGreetingLoading: StateFlow<Boolean> = _isGreetingLoading.asStateFlow()

    private val _activeScreen = MutableStateFlow(Screen.TODAY)
    val activeScreen: StateFlow<Screen> = _activeScreen.asStateFlow()

    private val _triggerBreathing = MutableStateFlow(false)
    val triggerBreathing: StateFlow<Boolean> = _triggerBreathing.asStateFlow()

    private val _triggerComposer = MutableStateFlow(false)
    val triggerComposer: StateFlow<Boolean> = _triggerComposer.asStateFlow()

    private val _preferredBreathingTechnique = MutableStateFlow<String?>(null)
    val preferredBreathingTechnique: StateFlow<String?> = _preferredBreathingTechnique.asStateFlow()

    private val _activeColorPalette = MutableStateFlow("PEACH")
    val activeColorPalette: StateFlow<String> = _activeColorPalette.asStateFlow()

    enum class StartupGreetingType {
        MORNING, EVENING, LATE_NIGHT
    }

    private val _showStartupGreeting = MutableStateFlow<StartupGreetingType?>(null)
    val showStartupGreeting: StateFlow<StartupGreetingType?> = _showStartupGreeting.asStateFlow()

    fun dismissStartupGreeting() {
        _showStartupGreeting.value = null
    }

    fun setColorPalette(palette: String) {
        _activeColorPalette.value = palette
        val prefs = getApplication<Application>().getSharedPreferences("lumee_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("active_color_palette", palette).apply()
    }

    fun checkAndTriggerStartupGreeting() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val dateString = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(calendar.time)
        val prefs = getApplication<Application>().getSharedPreferences("startup_greetings", android.content.Context.MODE_PRIVATE)

        val targetType: StartupGreetingType? = when (hour) {
            in 4..11 -> StartupGreetingType.MORNING
            in 17..20 -> StartupGreetingType.EVENING
            in 21..23 -> StartupGreetingType.LATE_NIGHT
            in 0..3 -> StartupGreetingType.LATE_NIGHT
            else -> null
        }

        if (targetType != null) {
            val key = "last_greeting_${targetType.name}_$dateString"
            val alreadyShown = prefs.getBoolean(key, false)
            if (!alreadyShown) {
                prefs.edit().putBoolean(key, true).apply()
                _showStartupGreeting.value = targetType
            }
        }
    }

    fun setTriggerBreathing(trigger: Boolean) {
        _triggerBreathing.value = trigger
    }

    fun setTriggerComposer(trigger: Boolean) {
        _triggerComposer.value = trigger
    }

    fun setPreferredBreathingTechnique(technique: String?) {
        _preferredBreathingTechnique.value = technique
    }

    fun handleNotificationIntent(intent: android.content.Intent?) {
        if (intent == null) return
        val navigateTo = intent.getStringExtra("NAVIGATE_TO")
        val triggerBreathe = intent.getBooleanExtra("TRIGGER_BREATHING", false)
        val triggerComp = intent.getBooleanExtra("TRIGGER_COMPOSER", false)
        val technique = intent.getStringExtra("BREATHING_TECHNIQUE")

        if (navigateTo != null) {
            when (navigateTo) {
                "TODAY" -> navigateTo(Screen.TODAY)
                "REFLECTIONS" -> navigateTo(Screen.REFLECTIONS)
                "QUOTES" -> navigateTo(Screen.QUOTES)
                "SETTINGS" -> navigateTo(Screen.SETTINGS)
            }
        }

        if (triggerBreathe) {
            _triggerBreathing.value = true
            if (technique != null) {
                _preferredBreathingTechnique.value = technique
            }
        }
        if (triggerComp) {
            _triggerComposer.value = true
        }

        try {
            intent.removeExtra("NAVIGATE_TO")
            intent.removeExtra("TRIGGER_BREATHING")
            intent.removeExtra("TRIGGER_COMPOSER")
            intent.removeExtra("BREATHING_TECHNIQUE")
        } catch (_: Exception) {}
    }

    // Reflections Prompt Handling
    private val prompts = listOf(
        "What is on your mind right now?",
        "What do you want to remember about today?",
        "What made you pause today?",
        "What are you grateful for in this moment?",
        "What do you wish your future self remembered?"
    )
    private val _currentPrompt = MutableStateFlow(prompts.first())
    val currentPrompt: StateFlow<String> = _currentPrompt.asStateFlow()

    enum class Screen {
        TODAY, REFLECTIONS, QUOTES, SETTINGS
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = LumeeRepository(database.lumeeDao())

        val prefs = application.getSharedPreferences("lumee_prefs", android.content.Context.MODE_PRIVATE)
        _activeColorPalette.value = prefs.getString("active_color_palette", "PEACH") ?: "PEACH"
        
        userProfile = repository.userProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        moments = repository.moments.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        emotions = repository.emotions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        refreshTodayGreeting()
        NotificationScheduler.scheduleNextNotification(application)
    }

    fun navigateTo(screen: Screen) {
        _activeScreen.value = screen
    }

    fun refreshTodayGreeting(force: Boolean = false) {
        viewModelScope.launch {
            _isGreetingLoading.value = true
            try {
                val greetingResult = repository.resolveTodayGreeting(forceRefresh = force)
                _todayGreeting.value = greetingResult
            } catch (e: Exception) {
                _todayGreeting.value = Pair("Good morning. Take a deep breath. Focus on peace today.", "Supportive Note")
            } finally {
                _isGreetingLoading.value = false
            }
        }
    }

    fun saveMoment(
        content: String,
        usePrompt: Boolean,
        imageUri: String? = null,
        videoUri: String? = null,
        audioUri: String? = null
    ) {
        viewModelScope.launch {
            val promptText = if (usePrompt) _currentPrompt.value else null
            repository.saveMoment(content, promptText, imageUri, videoUri, audioUri)
            nextPrompt() // Cycle prompt after successful save
        }
    }

    fun deleteMoment(moment: MomentEntity) {
        viewModelScope.launch {
            repository.deleteMoment(moment)
        }
    }

    fun updateProfileName(name: String) {
        viewModelScope.launch {
            repository.updateProfileName(name)
        }
    }

    fun updateProfileBirthday(month: Int?, day: Int?) {
        viewModelScope.launch {
            repository.updateProfileBirthday(month, day)
        }
    }

    fun updateReminderTime(time: String?) {
        viewModelScope.launch {
            repository.updateReminderTime(time)
            if (time != null) {
                NotificationScheduler.scheduleNextNotification(getApplication())
            } else {
                NotificationScheduler.cancelNotification(getApplication())
            }
        }
    }

    fun nextPrompt() {
        val currentIndex = prompts.indexOf(_currentPrompt.value)
        val nextIndex = (currentIndex + 1) % prompts.size
        _currentPrompt.value = prompts[nextIndex]
    }

    fun sendEmotion(text: String, emotionType: String? = null) {
        viewModelScope.launch {
            if (text.trim().isEmpty()) return@launch
            // Insert user emotion
            repository.insertEmotion("USER", text, emotionType)
            
            // Craft a highly organic, warm, comforting and poetic response
            val buddyResponse = when {
                text.lowercase().contains("sad") || text.lowercase().contains("hurt") || text.lowercase().contains("cry") || text.lowercase().contains("grief") || text.lowercase().contains("lonely") -> {
                    "I hear your sadness, friend. Remember that clouds are transient, yet the blue sky above them never leaves. Rest your head, you are completely safe here. 🌸"
                }
                text.lowercase().contains("angry") || text.lowercase().contains("mad") || text.lowercase().contains("hate") || text.lowercase().contains("annoyed") || text.lowercase().contains("furious") -> {
                    "It is natural to feel a storm inside sometimes. Let the fire breathe without burning you. I am here sitting next to you in peace. 🍃"
                }
                text.lowercase().contains("happy") || text.lowercase().contains("glad") || text.lowercase().contains("joy") || text.lowercase().contains("excited") || text.lowercase().contains("great") -> {
                    "Your joy warms my heart like beautiful sunbeams. Let us cherish this light and store it gently in our souls. ✨"
                }
                text.lowercase().contains("anxious") || text.lowercase().contains("scared") || text.lowercase().contains("fear") || text.lowercase().contains("stress") || text.lowercase().contains("worry") -> {
                    "Exhale slowly... inhale peace. The present moment is small and safe. You do not have to carry tomorrow's weight today. 🌊"
                }
                else -> {
                    "Thank you for sharing your heart with me. Every emotion is a guest passing through. Let us sit together in pure acceptance. 🕊️"
                }
            }
            // Add buddy response slightly after for that sweet realistic response effect
            kotlinx.coroutines.delay(600)
            repository.insertEmotion("BUDDY", buddyResponse, null)
        }
    }

    fun clearEmotionHistory() {
        viewModelScope.launch {
            repository.clearEmotions()
        }
    }

    fun updatePassword(password: String?) {
        viewModelScope.launch {
            repository.updateProfilePassword(password)
        }
    }
}
