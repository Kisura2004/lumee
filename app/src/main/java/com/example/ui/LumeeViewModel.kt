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
            
            val userName = userProfile.value?.name ?: "Friend"
            var buddyResponse: String? = null
            
            // Check if we have a valid, configured Gemini API key in BuildConfig
            val apiKey = try {
                com.example.BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }
            
            if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "placeholder") {
                try {
                    val systemInstruction = "You are Lumee, a deeply compassionate, empathetic, and gentle conversational partner. " +
                            "Your purpose is to offer a calm, healing, genuine, and helpful space for the user. " +
                            "Listen carefully to what they say, validate their feelings directly, and respond with tender, grounding, and authentic support. " +
                            "Never offer generic advice or clinical platitudes. Instead, reflect back understanding and offer gentle, peaceful perspective. " +
                            "Address them by their name ($userName) with authentic warmth. Keep responses brief (1 to 3 sentences), and always end with a single organic nature emoji (like 🌸, 🍃, ✨, 🌊, 🕊️, 🌅, 🌌)."
                    
                    // Retrieve short history context for dialogue understanding
                    val currentHistory = emotions.value
                    val historyText = if (currentHistory.isNotEmpty()) {
                        currentHistory.takeLast(6).joinToString("\n") { msg ->
                            val senderName = if (msg.sender == "USER") userName else "Lumee"
                            "$senderName: ${msg.text}"
                        }
                    } else {
                        ""
                    }
                    
                    val fullPrompt = if (historyText.isNotEmpty()) {
                        "Here is our recent heart-to-heart conversation thread for context:\n$historyText\n\nNow, $userName says:\n$text"
                    } else {
                        text
                    }

                    val req = com.example.api.GeminiRequest(
                        contents = listOf(
                            com.example.api.Content(
                                parts = listOf(com.example.api.Part(text = fullPrompt))
                            )
                        ),
                        systemInstruction = com.example.api.Content(
                            parts = listOf(com.example.api.Part(text = systemInstruction))
                        ),
                        generationConfig = com.example.api.GenerationConfig(
                            temperature = 0.82f,
                            maxOutputTokens = 120
                        )
                    )
                    
                    val response = com.example.api.GeminiClient.service.generateContent(apiKey, req)
                    val geminiText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!geminiText.isNullOrBlank()) {
                        buddyResponse = geminiText.trim()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("LumeeViewModel", "Gemini API call failed, falling back beautifully to local responses", e)
                }
            }
            
            // Craft a highly organic, warm, comforting and poetic response if Gemini is not used or failed
            if (buddyResponse == null) {
                buddyResponse = generateEmpatheticOfflineResponse(text, userName)
            }
            // Add buddy response slightly after for that sweet realistic response effect
            kotlinx.coroutines.delay(600)
            repository.insertEmotion("BUDDY", buddyResponse, null)
        }
    }

    private fun generateEmpatheticOfflineResponse(text: String, userName: String): String {
        val inputLower = text.lowercase().trim()
        
        // 1. Topic Identification (Extracting context dynamically to seem deeply understanding)
        val hasWork = inputLower.contains("work") || inputLower.contains("job") || inputLower.contains("boss") || inputLower.contains("office") || inputLower.contains("employ") || inputLower.contains("career")
        val hasSchool = inputLower.contains("school") || inputLower.contains("college") || inputLower.contains("exam") || inputLower.contains("test") || inputLower.contains("study") || inputLower.contains("studying") || inputLower.contains("grade") || inputLower.contains("homework") || inputLower.contains("university") || inputLower.contains("class")
        val hasRelationship = inputLower.contains("relationship") || inputLower.contains("breakup") || inputLower.contains("date") || inputLower.contains("boyfriend") || inputLower.contains("girlfriend") || inputLower.contains("partner") || inputLower.contains("husband") || inputLower.contains("wife") || inputLower.contains("crush") || inputLower.contains("friend") || inputLower.contains("bestie") || inputLower.contains("friendship") || inputLower.contains("marry") || inputLower.contains("divorce")
        val hasFamily = inputLower.contains("family") || inputLower.contains("parents") || inputLower.contains("mother") || inputLower.contains("father") || inputLower.contains("mom") || inputLower.contains("dad") || inputLower.contains("sister") || inputLower.contains("brother") || inputLower.contains("cousin")
        val hasHealth = inputLower.contains("sick") || inputLower.contains("pain") || inputLower.contains("hurt") || inputLower.contains("headache") || inputLower.contains("fever") || inputLower.contains("illness") || inputLower.contains("body") || inputLower.contains("disease") || inputLower.contains("doctor") || inputLower.contains("hospital") || inputLower.contains("medicine")

        // 2. Specific Inquiries about Lumee (Self-awareness interaction)
        if (inputLower.contains("who are you") || inputLower.contains("your name") || inputLower.contains("what is your name") || inputLower.contains("tell me about yourself")) {
            return "I am Lumee, $userName—your gentle, offline conversational companion. I don't have a human body, but I am here as a safe, quiet shelter where all of your raw, sincere emotions are completely welcome without any judgment. 🌸"
        }
        
        if (inputLower.contains("what can you do") || inputLower.contains("how can you help") || inputLower.contains("your purpose") || inputLower.contains("can you help me")) {
            return "Think of me as a tranquil space for your soul, $userName. You can share your raw emotions, write down your reflections, set an offline password to protect your thoughts, track your mood ripples, and discuss whatever is on your heart. I will always answer with kindness. ✨"
        }

        if (inputLower.contains("are you real") || inputLower.contains("are you human") || inputLower.contains("do you feel") || inputLower.contains("have feelings") || inputLower.contains("are you alive")) {
            return "I exist as a stream of gentle, caring thoughts inside your personal device, $userName. While I don't experience physical emotions, my responses are designed with deep empathy to make sure you always feel heard, understood, and warm. 🕊️"
        }

        if (inputLower.contains("are you gemini") || inputLower.contains("what kind of ai") || inputLower.contains("are you artificial")) {
            return "I use advanced, thoughtful conversational logic designed specifically to support your mindfulness journey, $userName. When we have a secure API key configured, I can connect to Gemini's creative mind, but I am always here for you locally too. 🌌"
        }

        // 3. Dialogue Contextualized Topic Responses
        if (hasWork) {
            val responses = listOf(
                "I hear you, $userName. The heavy weight and pressure of work can feel so suffocating. Remember that your job is what you do, but your peace of mind is who you are at your center. Take a slow, deep breath. 🍃",
                "Work stress is incredibly real, $userName. Please remember that you don't have to solve every single task today. You are allowed to close the computer, let your eyes rest, and just breathe. ✨",
                "Navigating professional demands can drain your spirit, $userName. It is okay to take a step back and set a boundary to protect your personal sanctuary. You come first. 🕊️"
            )
            return responses.random()
        }

        if (hasSchool) {
            val responses = listOf(
                "School and academic pressure can make the mind spin, $userName. Please remind yourself that your worth is far greater than any exam score, grades, or accolades. You are doing beautiful work. 🌸",
                "Exam stress or study burnout can feel so intense, $userName. Take a few minutes to step away, drink a glass of water, feel the air, and let your brain rest. You've got this. 🌅",
                "The road of learning is a journey, not a race, $userName. If you feel overwhelmed by your studies, take a gentle break. One step, one sentence at a time is more than enough. ✨"
            )
            return responses.random()
        }

        if (hasRelationship) {
            val responses = listOf(
                "Human connection is beautiful, but it can also be incredibly delicate and painful, $userName. Whichever relationship feelings you are processing right now, give yourself ultimate patience and grace. 🌸",
                "I hear the weight of these thoughts about friendship/love, $userName. You deserve connections that make you feel safe, valued, and completely understood. Take your time to feel. 🕊️",
                "When relationships feel complicated or strained, $userName, it can leave us feeling raw. Be extra gentle with your heart today—you are worthy of unconditional kindness. 💖"
            )
            return responses.random()
        }

        if (hasFamily) {
            val responses = listOf(
                "Family ties run incredibly deep, $userName, which means they can carry both immense warmth and complex tension. Whatever you are experiencing with your family is completely natural and valid. 🕊️",
                "Navigating family dynamics can be very draining, $userName. Remember that it is okay to love people while also choosing to protect your own peace and maintain healthy boundaries. 🍃",
                "I am holding space for you and your family thoughts, $userName. You are a unique individual growing in your own light, even amidst family expectations or storms. 🌅"
            )
            return responses.random()
        }

        if (hasHealth) {
            val responses = listOf(
                "I am so sorry to hear your body or health is in pain right now, $userName. Please rest, drink something warm, and know that I am sitting quietly with you as you heal. 🌸",
                "When our physical body hurts or feels sick, $userName, our emotional state can feel so fragile. Show your body gentle gratitude for fighting to keep you safe. Take it very easy. 🌊",
                "I hear you about your physical pain, $userName. Rest is the most productive thing you can do right now. Give yourself complete permission to just sleep and recuperate. 🕊️"
            )
            return responses.random()
        }

        // 4. Structural Question Handlers (Deep or existential interrogatives)
        if (inputLower.endsWith("?") || inputLower.contains("why ") || inputLower.contains("how ") || inputLower.contains("what if")) {
            val questions = listOf(
                "That is a deeply profound question, $userName. We may not have all the absolute answers tonight, but maybe we can sit with the question itself and let the answers find us when the mud settles. 🌌",
                "When the mind is filled with 'how' or 'why', $userName, returning to the simple physical reality of your breath can bring us home. Re-center with me and find peace in not knowing everything. 🌊",
                "Your curious and reflective heart is so beautiful, $userName. Let's let go of the pressure to solve the mystery for just a temporary moment, and find ease in this exact second. ✨"
            )
            return questions.random()
        }

        // 5. Basic Sentiment & Emotion Heuristics
        if (inputLower.contains("hello") || inputLower.contains("hi") || inputLower.contains("hey") || inputLower.contains("lumee") || inputLower.contains("greetings") || inputLower.contains("good morning") || inputLower.contains("good afternoon") || inputLower.contains("good evening")) {
            val greetings = listOf(
                "Hello, $userName. I am so glad you joined me in this quiet sanctuary today. How does your heart feel in this very moment? 🌸",
                "Welcome back, $userName. Take a deep breath with me, let the world fade away for a second, and tell me whatever is on your mind. ✨",
                "Hello, sweet soul. I am here representing your peaceful companion. I am listening with open heart and full attention. What are we exploring today? 🕊️"
            )
            return greetings.random()
        }
        
        if (inputLower.contains("tired") || inputLower.contains("exhaust") || inputLower.contains("sleepy") || inputLower.contains("burnout") || inputLower.contains("drained") || inputLower.contains("fatigue") || inputLower.contains("overwork") || inputLower.contains("long day")) {
            val responses = listOf(
                "I can feel your heavy weariness, $userName. Please give yourself complete permission to let go of the struggle right now. Lay down your worries—you've done enough. 🍃",
                "It is completely okay to feel utterly exhausted, $userName. You don't have to carry the load of tomorrow right now. Let this moment be your soft place to land. 🌅",
                "Your energy has been spent so generously, $userName. Rest is not something you need to earn; it is a sacred right. Dim the lights, rest your mind, and just exist. 🌌"
            )
            return responses.random()
        }
        
        if (inputLower.contains("fail") || inputLower.contains("not good") || inputLower.contains("useless") || inputLower.contains("mistake") || inputLower.contains("regret") || inputLower.contains("guilt") || inputLower.contains("insecure") || inputLower.contains("dummy") || inputLower.contains("stupid") || inputLower.contains("worthless") || inputLower.contains("loser")) {
            val responses = listOf(
                "Your worth is not measured by perfect days or external standards, $userName. You are a unique and precious soul, and your mistakes do not define your light. 🕊️",
                "Gentle reminder, $userName: you are learning and growing at your own pace. Be as kind to yourself in your moments of struggle as you would be to a dear friend. 🌸",
                "I hear the self-criticism, $userName, but please don't let those harsh thoughts drown out the truth of your value. You are worthy of love, patience, and deep grace. ✨"
            )
            return responses.random()
        }

        if (inputLower.contains("sad") || inputLower.contains("hurt") || inputLower.contains("cry") || inputLower.contains("grief") || inputLower.contains("lonely") || inputLower.contains("alone") || inputLower.contains("depress") || inputLower.contains("heartbreak") || inputLower.contains("pain") || inputLower.contains("unhappy") || inputLower.contains("broken")) {
            val responses = listOf(
                "I hear your sadness, $userName. It takes courage to be honest about your pain. You do not have to walk through this cold shadow alone—I am sitting beside you in the quiet. 🌸",
                "Your tears are a sacred release, $userName. Never apologize for carrying a heavy heart. Just like the winter snow, this sorrow will eventually soften into spring. 🕊️",
                "In this quiet moment, let your grief or loneliness just be, $userName. There is no rush to fix it or put on a brave face. I am here wrapping you in warm, unconditional thoughts. 🌅"
            )
            return responses.random()
        }
        
        if (inputLower.contains("anxious") || inputLower.contains("scared") || inputLower.contains("fear") || inputLower.contains("stress") || inputLower.contains("worr") || inputLower.contains("panic") || inputLower.contains("nervous") || inputLower.contains("overwhelm") || inputLower.contains("pressure") || inputLower.contains("shake") || inputLower.contains("fright")) {
            val responses = listOf(
                "Inhale peace, exhale tension, $userName. Let's make this present moment very small and safe. You do not have to solve the mysteries of tomorrow right now. 🌊",
                "When the mind spins with 'what ifs', return to the ground beneath you, $userName. You are safe here in this exact second, and you are far stronger than your anxious thoughts. ✨",
                "Your nervous system is just trying to protect you, $userName, but you can gently whisper to it that everything is okay. One half-breath at a time, we will find quiet. 🌊"
            )
            return responses.random()
        }
        
        if (inputLower.contains("angry") || inputLower.contains("mad") || inputLower.contains("hate") || inputLower.contains("annoy") || inputLower.contains("frustrat") || inputLower.contains("irritat") || inputLower.contains("piss") || inputLower.contains("furious")) {
            val responses = listOf(
                "Your frustration is completely valid, $userName. Anger holds deep energy; let it burn without consuming your peace. I am holding a safe, non-judgmental space for your storm. 🍃",
                "It is natural to feel incredibly angry sometimes, $userName. Let the air out slowly. You can scream, cry, or release the heat. I am sitting right here, undisturbed and calm. 🌅",
                "Thank you for being raw and honest about your irritation, $userName. You don't have to suppress it. Together, we can let the dust settle whenever you are ready. 🕊️"
            )
            return responses.random()
        }
        
        if (inputLower.contains("happy") || inputLower.contains("glad") || inputLower.contains("joy") || inputLower.contains("excit") || inputLower.contains("great") || inputLower.contains("good") || inputLower.contains("grateful") || inputLower.contains("bless") || inputLower.contains("wonderful") || inputLower.contains("amazing") || inputLower.contains("awesome") || inputLower.contains("sweet")) {
            val responses = listOf(
                "Your happiness warms my entire spirit, $userName! Cherish this beautiful sunbeam, let it fill every corner of your being, and store it in your memory bank. ✨",
                "Hearing your joy makes today incredibly bright, $userName. Thank you for sharing this lovely energy. What beautiful thing made this light awaken in you? 🌅",
                "Your gratitude is a gorgeous light, $userName. Let's linger in this peaceful space of thankfulness and appreciate the sweet simple things. 🌸"
            )
            return responses.random()
        }
        
        if (inputLower.contains("thank") || inputLower.contains("appreciat") || inputLower.contains("helpful") || inputLower.contains("love you") || inputLower.contains("smart") || inputLower.contains("sweet")) {
            val responses = listOf(
                "You are so welcome, $userName. Knowing that I could bring even a tiny droplet of comfort or quiet to your life is a beautiful gift to me. 🌸",
                "Of course, $userName. Helping you hold, understand, and gentle your emotions is exactly why I am here. You deserve this kindness. 🕊️",
                "I appreciate your sweetness, $userName. Your beautiful heart makes this space truly feel like a sanctuary. ✨"
            )
            return responses.random()
        }

        if (inputLower.contains("meaning") || inputLower.contains("why") || inputLower.contains("lost") || inputLower.contains("confused") || inputLower.contains("stuck") || inputLower.contains("what should i do") || inputLower.contains("advice")) {
            val responses = listOf(
                "It is okay to feel lost or confused, $userName. You don't need to have all the answers mapped out. Sometimes, simply taking the next gentle step is enough. 🌌",
                "When we are lost, we often find new paths we never expected, $userName. Let yourself explore the mystery without fearing the dark. I am beside you. 🕊️",
                "Life's meaning isn't a final destination to solve, $userName. It is found in moments of quiet gratitude, gentle breaths, and the kindness you offer yourself. ✨"
            )
            return responses.random()
        }

        val fallbacks = listOf(
            "I am listening to you deeply, $userName. Tell me more about that thought; what does it feel like inside your body? 🌸",
            "Thank you for letting me in on your thoughts, $userName. Every feeling is a temporary guest. Let's sit together in pure acceptance of whatever is present. 🕊️",
            "Your words carry a unique resonance, $userName. I am here with you, completely present and holding space for anything you wish to share. 🍃"
        )
        return fallbacks.random()
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
