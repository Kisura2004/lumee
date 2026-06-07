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
