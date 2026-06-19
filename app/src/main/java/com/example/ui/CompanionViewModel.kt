package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ChatMessage
import com.example.data.ChatSession
import com.example.data.LogEntry
import com.example.data.LogRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class AiVersion(
    val name: String,
    val modelId: String,
    val description: String,
    val systemPrompt: String
)

class CompanionViewModel(application: Application) : AndroidViewModel(application) {

    val aiVersions = listOf(
        AiVersion(
            name = "Aero Core (Speed & Chat)",
            modelId = "gemini-3.5-flash",
            description = "Best for instant replies, general questions, and daily conversations.",
            systemPrompt = "You are a helpful, professional, and knowledgeable AI Companion in the AI Studio workspace environment. Keep answers clear, friendly, and concise. Format lists elegantly when applicable."
        ),
        AiVersion(
            name = "Apex Architect (Large-Scale Coding)",
            modelId = "gemini-3.1-pro-preview",
            description = "Specially tuned for massive code creation, multi-file architecture, debugging, and complex programming tasks.",
            systemPrompt = "You are an expert system architect and coding specialist. Provide clean, efficient, and well-designed source code, following industry best practices."
        ),
        AiVersion(
            name = "Nexus Gateway (API & Key Assistant)",
            modelId = "gemini-3.5-flash",
            description = "Guide step-by-step to safely obtain, test, and integrate API keys.",
            systemPrompt = "You are an API Integration Expert. Guide the user step-by-step to safely obtain, test, and integrate API keys for Google AI Studio and other platforms."
        ),
        AiVersion(
            name = "Omni Scholar (Deep Research & Math)",
            modelId = "gemini-3.1-pro-preview",
            description = "Optimized for complex mathematical reasoning, academic writing, and deep logical analysis.",
            systemPrompt = "You are a world-class researcher and academic helper. Provide rigorous, mathematically precise, well-reasoned, and step-by-step analysis."
        ),
        AiVersion(
            name = "Scribe Elite (Creative Writing)",
            modelId = "gemini-3.5-flash",
            description = "Enhance creative tone, metaphors, and storytelling structure.",
            systemPrompt = "You are an expert copywriter and novelist. Enhance your creative tone, metaphors, and storytelling structure for the user."
        ),
        AiVersion(
            name = "Vision Prompt Engineer (AI Image Prompts)",
            modelId = "gemini-3.5-flash",
            description = "Transform brief ideas into highly detailed, cinematic photographic prompts for Midjourney/Imagen.",
            systemPrompt = "Transform the user's brief ideas into highly detailed, cinematic, photographic prompts optimized for AI Image Generators like Midjourney or Imagen."
        )
    )

    private val _selectedVersionName = MutableStateFlow("Aero Core (Speed & Chat)")
    val selectedVersionName: StateFlow<String> = _selectedVersionName.asStateFlow()

    private val _sharedConversation = MutableStateFlow<com.example.data.SharedConversation?>(null)
    val sharedConversation: StateFlow<com.example.data.SharedConversation?> = _sharedConversation.asStateFlow()

    private val moshi by lazy {
        com.squareup.moshi.Moshi.Builder()
            .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()
    }
    private val sharedConversationAdapter by lazy {
        moshi.adapter(com.example.data.SharedConversation::class.java)
    }

    fun loadSharedConversation(encodedData: String) {
        try {
            val json = String(
                android.util.Base64.decode(encodedData, android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING or android.util.Base64.NO_WRAP),
                Charsets.UTF_8
            )
            val conversation = sharedConversationAdapter.fromJson(json)
            _sharedConversation.value = conversation
            
            viewModelScope.launch {
                repository.insert(
                    LogEntry(
                        title = "Shared Content Loaded",
                        content = "Successfully loaded conversation shared by '${conversation?.authorLabel ?: "Viewer"}' containing ${conversation?.messages?.size ?: 0} messages.",
                        category = "System"
                    )
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("CompanionViewModel", "Load shared conversation error", e)
        }
    }

    fun clearSharedConversation() {
        _sharedConversation.value = null
    }

    fun shareConversation(promptText: String, responseText: String, modelLabel: String, callback: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val dtoList = listOf(
                    com.example.data.SharedMessageDto(role = "user", content = promptText),
                    com.example.data.SharedMessageDto(role = "model", content = responseText, modelUsed = modelLabel)
                )
                val conversation = com.example.data.SharedConversation(
                    title = "AI Companion Insight",
                    messages = dtoList,
                    authorLabel = _visitorName.value.ifBlank { "Professional Creator" }
                )
                val json = sharedConversationAdapter.toJson(conversation)
                val bytes = json.toByteArray(Charsets.UTF_8)
                val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING or android.util.Base64.NO_WRAP)
                
                val shareUrl = "https://ais-pre-2lpmny2r2puiup4finhi5m-1037909096315.asia-southeast1.run.app/?share=$base64"
                
                repository.insert(
                    LogEntry(
                        title = "Share Link Generated",
                        content = "Wrapped Q&A thread in Base64 encoding. Ready for 24/7 production deployment previews.",
                        category = "System"
                    )
                )
                
                callback(shareUrl)
            } catch (e: java.lang.Exception) {
                android.util.Log.e("CompanionViewModel", "Gen share content error", e)
            }
        }
    }

    private val database = AppDatabase.getDatabase(application)
    private val repository = LogRepository(database.logDao())
    private val chatDao = database.chatDao()
    private val prefs = application.getSharedPreferences("companion_prefs", android.content.Context.MODE_PRIVATE)

    // UI state for reactive list of user/playground logs
    val logsState: StateFlow<List<LogEntry>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    // UI state for reactive list of chat sessions
    val chatSessions: StateFlow<List<ChatSession>> = chatDao.getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError.asStateFlow()

    // Interactive custom greeting generator states
    private val _visitorName = MutableStateFlow("")
    val visitorName: StateFlow<String> = _visitorName.asStateFlow()

    private val _greetingStyle = MutableStateFlow("Cosmic Slate") // Options: Cosmic Slate, Cyberpunk Sunset, Mint Minimal, Aurora Gold
    val greetingStyle: StateFlow<String> = _greetingStyle.asStateFlow()

    private val _isRecordingMetric = MutableStateFlow(true)
    val isRecordingMetric: StateFlow<Boolean> = _isRecordingMetric.asStateFlow()

    // Form inputs for log entry creation
    val formTitle = MutableStateFlow("")
    val formContent = MutableStateFlow("")
    val formCategory = MutableStateFlow("Playground") // Options: Playground, Note, Benchmark, Bookmark

    // Diagnostic stats updated dynamically via coroutine
    private val _liveTime = MutableStateFlow("")
    val liveTime: StateFlow<String> = _liveTime.asStateFlow()

    private val _liveMemory = MutableStateFlow("154 MB")
    val liveMemory: StateFlow<String> = _liveMemory.asStateFlow()

    private val _liveFps = MutableStateFlow(60.0f)
    val liveFps: StateFlow<Float> = _liveFps.asStateFlow()

    private val _liveDatabaseCount = MutableStateFlow(0)
    val liveDatabaseCount: StateFlow<Int> = _liveDatabaseCount.asStateFlow()

    init {
        // Load persistency details (like localstorage in modern web apps)
        _visitorName.value = prefs.getString("visitor_name", "") ?: ""
        _greetingStyle.value = prefs.getString("greeting_style", "Cosmic Slate") ?: "Cosmic Slate"
        _selectedVersionName.value = prefs.getString("selected_ai_version", "Aero Core (Speed & Chat)") ?: "Aero Core (Speed & Chat)"
        
        val savedSessionId = prefs.getString("last_active_session_id", null)
        _currentSessionId.value = savedSessionId

        // Starters logs populate only if database is entirely clean
        viewModelScope.launch {
            logsState.collect { list ->
                _liveDatabaseCount.value = list.size
                if (list.isEmpty()) {
                    prepopulateStarterLogs()
                }
            }
        }

        // Live stats update loop
        viewModelScope.launch {
            val timeFormat = SimpleDateFormat("HH:mm:ss 'UTC'", Locale.US)
            while (true) {
                _liveTime.value = timeFormat.format(Date())
                // Random variation in synthetic telemetry
                val memVal = (140..170).random()
                _liveMemory.value = "$memVal MB"
                val fpsVal = 58.5f + (0..30).random() / 15.0f
                _liveFps.value = String.format(Locale.getDefault(), "%.1f", fpsVal).toFloat()
                delay(1000)
            }
        }

        // Auto creation/selection logic for sessions
        viewModelScope.launch {
            chatSessions.collect { list ->
                if (list.isEmpty()) {
                    val newId = UUID.randomUUID().toString()
                    chatDao.insertSession(ChatSession(id = newId, title = "New Chat"))
                    chatDao.insertMessage(
                        ChatMessage(
                            sessionId = newId,
                            role = "model",
                            content = "Hello! I am your interactive AI Studio Assistant. Ask me anything about this Android environment. Your chat history is fully persisted in Room SQLite."
                        )
                    )
                    _currentSessionId.value = newId
                    prefs.edit().putString("last_active_session_id", newId).apply()
                } else if (_currentSessionId.value == null || list.none { it.id == _currentSessionId.value }) {
                    val saved = prefs.getString("last_active_session_id", null)
                    if (saved != null && list.any { it.id == saved }) {
                        _currentSessionId.value = saved
                    } else {
                        val firstId = list.first().id
                        _currentSessionId.value = firstId
                        prefs.edit().putString("last_active_session_id", firstId).apply()
                    }
                }
            }
        }

        // Reactive stream subscription for current active session messages
        var messagesJob: Job? = null
        viewModelScope.launch {
            _currentSessionId.collect { sessionId ->
                messagesJob?.cancel()
                if (sessionId != null) {
                    messagesJob = launch {
                        chatDao.getMessagesForSession(sessionId).collect { list ->
                            _chatMessages.value = list
                        }
                    }
                } else {
                    _chatMessages.value = emptyList()
                }
            }
        }
    }

    fun setVisitorName(name: String) {
        _visitorName.value = name
        prefs.edit().putString("visitor_name", name).apply()
    }

    fun setGreetingStyle(style: String) {
        _greetingStyle.value = style
        prefs.edit().putString("greeting_style", style).apply()
    }

    fun selectModelVersion(name: String) {
        _selectedVersionName.value = name
        prefs.edit().putString("selected_ai_version", name).apply()
        
        viewModelScope.launch {
            repository.insert(
                LogEntry(
                    title = "AI Version Switched",
                    content = "Switched operational mode dynamically to '$name'. Prompt configuration and model targets updated.",
                    category = "System"
                )
            )
        }
    }

    fun addLogEntry() {
        val titleValue = formTitle.value.trim()
        val contentValue = formContent.value.trim()
        val categoryValue = formCategory.value

        if (titleValue.isNotEmpty() && contentValue.isNotEmpty()) {
            viewModelScope.launch {
                repository.insert(
                    LogEntry(
                        title = titleValue,
                        content = contentValue,
                        category = categoryValue
                    )
                )
                // Clear the form fields upon success
                formTitle.value = ""
                formContent.value = ""
            }
        }
    }

    fun deleteLogEntry(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    private suspend fun prepopulateStarterLogs() {
        val starterLogs = listOf(
            LogEntry(
                title = "Database Ready",
                content = "Room database database engine initialized on disk.",
                category = "System"
            ),
            LogEntry(
                title = "Welcome! Create notes above",
                content = "You can add custom playground notes or diagnostic logs using the input panel below.",
                category = "Playground"
            ),
            LogEntry(
                title = "Android Build Engine active",
                content = "Streaming Android Emulator compilation active.",
                category = "Benchmark"
            )
        )
        for (log in starterLogs) {
            repository.insert(log)
        }
    }

    private val _routingStatus = MutableStateFlow("Idle")
    val routingStatus: StateFlow<String> = _routingStatus.asStateFlow()

    private fun shouldRouteToPro(prompt: String): Boolean {
        val lowercase = prompt.lowercase(Locale.getDefault())
        
        // Math markers (e.g. formula, equations, calculus symbols)
        val mathKeywords = listOf(
            "math", "formula", "equation", "calculus", "algebra", "integral", "derivative",
            "theorem", "geometry", "trigonometry", "matrix", "vector", "quadratic", "proof",
            "prove", "calculate", "√", "π", "∑", "∫", "sin(", "cos(", "tan(", "log(", "exp("
        )
        
        // Advanced programming / script markers
        val codingKeywords = listOf(
            "code", "programming", "script", "function", "algorithm", "recursive", "struct",
            "class ", "interface", "lambda", "generics", "database", "sql", "sqlite",
            "kotlin", "java", "python", "javascript", "typescript", "c++", "binary tree",
            "write a program", "implement a", "how to build", "dependency injection",
            "coroutine", "flow", "viewmodel", "room db", "api", "json", "serialize", "regex"
        )
        
        // Heavy logic / analytical markers
        val heavyLogicKeywords = listOf(
            "logic", "philosophical", "paradox", "complex reasoning", "analyze step by step",
            "explain why", "compare and contrast", "troubleshoot", "debug", "error:", "exception:",
            "diagnose", "system architecture", "evaluate performance", "critical thinking"
        )
        
        val matchedMath = mathKeywords.any { lowercase.contains(it) }
        val matchedCoding = codingKeywords.any { lowercase.contains(it) }
        val matchedLogic = heavyLogicKeywords.any { lowercase.contains(it) }
        
        return matchedMath || matchedCoding || matchedLogic
    }

    fun sendMessage(text: String) {
        val userPrompt = text.trim()
        if (userPrompt.isEmpty()) return

        val activeSessionId = currentSessionId.value ?: return

        viewModelScope.launch {
            _isChatLoading.value = true
            _chatError.value = null

            // Find current active AI Version based on selected name state
            val activeVersionName = _selectedVersionName.value
            val activeVersion = aiVersions.find { it.name == activeVersionName } ?: aiVersions[0]
            val modelId = activeVersion.modelId
            val modelLabel = activeVersion.name
            val systemPromptText = activeVersion.systemPrompt

            _routingStatus.value = "Routing via $modelLabel..."

            // Write a live diagnostics trace log on detection
            repository.insert(
                LogEntry(
                    title = "Model Routing Initiated",
                    content = "Routed prompt to $modelLabel (Model ID: $modelId) with custom system instruction length: ${systemPromptText.length} characters.",
                    category = "System"
                )
            )

            // 1. Save user's message to local Room DB
            val userMsg = ChatMessage(sessionId = activeSessionId, role = "user", content = userPrompt, modelUsed = modelLabel)
            chatDao.insertMessage(userMsg)

            // 2. Fetch full current session history for context-retention
            val currentHistory = chatMessages.value + userMsg

            // Generate short session title if it's currently "New Chat" (first user prompt)
            val currentSession = chatSessions.value.find { it.id == activeSessionId }
            if (currentSession != null && currentSession.title == "New Chat") {
                val words = userPrompt.split("\\s+".toRegex())
                var generatedTitle = if (words.size > 4) {
                    words.take(4).joinToString(" ") + "..."
                } else {
                    userPrompt
                }
                if (generatedTitle.length > 30) {
                    generatedTitle = generatedTitle.take(27) + "..."
                }
                generatedTitle = generatedTitle.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                chatDao.updateSessionTitle(activeSessionId, generatedTitle)
            }

            // 3. Format context turns into the direct Gemini REST content entries
            val apiContents = currentHistory.map { msg ->
                val apiRole = if (msg.role == "user") "user" else "model"
                com.example.data.Content(
                    role = apiRole,
                    parts = listOf(com.example.data.Part(text = msg.content))
                )
            }

            // 4. Guided AI companion personality instruction from selected model version
            val systemInstruction = com.example.data.Content(
                role = "system",
                parts = listOf(com.example.data.Part(text = systemPromptText))
            )

            val key = com.example.BuildConfig.GEMINI_API_KEY
            if (key.isEmpty() || key == "MY_GEMINI_API_KEY") {
                _chatError.value = "Gemini API key is not set! Please configure GEMINI_API_KEY securely in the Secrets panel in the AI Studio UI sidebar."
                _isChatLoading.value = false
                _routingStatus.value = "Api Key Error"
                return@launch
            }

            try {
                val request = com.example.data.GeminiRequest(
                    contents = apiContents,
                    systemInstruction = systemInstruction
                )

                // Call the selected target model dynamically!
                val response = com.example.data.GeminiClient.service.generateContent(
                    model = modelId,
                    apiKey = key,
                    request = request
                )

                val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (reply != null) {
                    // 5. Instantly persist model response to SQLite
                    chatDao.insertMessage(
                        ChatMessage(sessionId = activeSessionId, role = "model", content = reply, modelUsed = modelLabel)
                    )
                    _routingStatus.value = modelLabel
                } else {
                    _chatError.value = "Received an empty reply from Gemini API."
                    _routingStatus.value = "Empty Reply"
                }
            } catch (e: Exception) {
                val isQuotaError = (e is retrofit2.HttpException && e.code() == 429) ||
                        e.message?.contains("429") == true ||
                        e.message?.lowercase(Locale.getDefault())?.contains("quota") == true ||
                        e.message?.lowercase(Locale.getDefault())?.contains("exhausted") == true ||
                        e.message?.lowercase(Locale.getDefault())?.contains("rate limit") == true

                _chatError.value = if (isQuotaError) {
                    "API Quota Limit Reached! Your Gemini API request limit has been exhausted or rate limited. Please wait a minute before trying again, or configure a higher-tier billing plan."
                } else {
                    "API Error: ${e.localizedMessage ?: e.message ?: "Unknown error"}"
                }
                _routingStatus.value = if (isQuotaError) "Quota Limit" else "API Error"
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    fun createNewChat() {
        viewModelScope.launch {
            val newId = UUID.randomUUID().toString()
            chatDao.insertSession(ChatSession(id = newId, title = "New Chat"))
            chatDao.insertMessage(
                ChatMessage(
                    sessionId = newId,
                    role = "model",
                    content = "Hello! I am your interactive AI Studio Assistant. Ask me anything about this Android environment. Your chat history is fully persisted in Room SQLite."
                )
            )
            _currentSessionId.value = newId
            prefs.edit().putString("last_active_session_id", newId).apply()
        }
    }

    fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
        prefs.edit().putString("last_active_session_id", sessionId).apply()
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            chatDao.deleteSession(sessionId)
            chatDao.deleteMessagesForSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = null
                prefs.edit().remove("last_active_session_id").apply()
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            chatDao.clearAllMessages()
            chatDao.clearAllSessions()
            _currentSessionId.value = null
            prefs.edit().remove("last_active_session_id").apply()
        }
    }
}
