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

    enum class StartupGreetingType {
        MORNING, EVENING
    }

    private var hasCheckedStartupGreeting = false

    private val _showStartupGreeting = MutableStateFlow<StartupGreetingType?>(null)
    val showStartupGreeting: StateFlow<StartupGreetingType?> = _showStartupGreeting.asStateFlow()

    fun dismissStartupGreeting() {
        _showStartupGreeting.value = null
    }

    fun checkAndTriggerStartupGreeting() {
        if (hasCheckedStartupGreeting) return
        hasCheckedStartupGreeting = true
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        if (hour in 5..11) {
            _showStartupGreeting.value = StartupGreetingType.MORNING
        } else if (hour >= 19 || hour < 5) {
            _showStartupGreeting.value = StartupGreetingType.EVENING
        } else {
            _showStartupGreeting.value = null
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
}
