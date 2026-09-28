package com.beakshield.voice

import com.beakshield.BeakShieldApp.preferences
import com.beakshield.dawson.Agent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VoiceHandler {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val engineContext = Dispatchers.Default.limitedParallelism(1)
    private val _modelHandler = VoiceModelHandler()

    private var tts: TtsEngine? = null
    private var stt: SttEngine? = null
    private val playback = AudioPlayback()
    private val capture = AudioCapture()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening = _isListening.asStateFlow()

    private val _speakResponses = MutableStateFlow(false)
    val speakResponses = _speakResponses.asStateFlow()

    val modelState = _modelHandler.state

    private data class SpeechItem(val text: String, val agentType: Agent.AgentType)
    private var speechQueue = Channel<SpeechItem>(Channel.UNLIMITED)
    private var speechWorker: Job? = null
    private var streamKey: String? = null
    private var streamCursor = 0

    private suspend fun ensureTts(): TtsEngine? = withContext(engineContext) {
        if (!_modelHandler.isReady) return@withContext null
        tts ?: TtsEngine(_modelHandler.localPath("kokoro")).also { tts = it }
    }

    private suspend fun ensureStt(): SttEngine? = withContext(engineContext) {
        if (!_modelHandler.isReady) return@withContext null
        stt ?: SttEngine(_modelHandler.localPath("whisper")).also { stt = it }
    }

    fun prewarmTts() {
        scope.launch {
            ensureTts()
        }
    }

    fun release() {
        stopSpeaking()
        tts?.release()
        tts = null
        stt?.release()
        stt = null
    }

    fun setSpeakResponses(enabled: Boolean) {
        _speakResponses.value = enabled
        if (enabled) prewarmTts() else stopSpeaking()
    }

    private fun voiceParams(agentType: Agent.AgentType): Triple<Int, Float, (FloatArray, Int) -> FloatArray> {
        return if (agentType == Agent.AgentType.DAWSON) {
            Triple(DAWSON_SID, DAWSON_SPEED, GravitasVoiceEffect::process)
        } else {
            Triple(SQUIREBOT_SID, SQUIREBOT_SPEED, RobotVoiceEffect::process)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun ensureSpeechWorker() {
        if (speechWorker?.isActive == true) return
        val queue = speechQueue
        speechWorker = scope.launch {
            val audioPipe = Channel<TtsAudio>(capacity = 1)   // 1-sentence prefetch
            launch {
                for (item in queue) {
                    val engine = ensureTts() ?: continue
                    val (sid, speed, effect) = voiceParams(item.agentType)
                    for (chunk in chunkForSpeech(item.text)) {
                        ensureActive()
                        val audio = engine.generate(chunk, sid, speed)
                        audioPipe.send(TtsAudio(effect(audio.samples, audio.sampleRate), audio.sampleRate))
                    }
                }
            }
            for (audio in audioPipe) {
                _isSpeaking.value = true
                playback.play(audio.samples, audio.sampleRate)
                if (audioPipe.isEmpty && queue.isEmpty) _isSpeaking.value = false
            }
        }
    }

    private fun enqueue(text: String, agentType: Agent.AgentType) {
        if (text.isBlank()) return
        ensureSpeechWorker()
        speechQueue.trySend(SpeechItem(text, agentType))
    }

    fun speak(text: String, agentType: Agent.AgentType) {
        stopSpeaking()
        enqueue(text, agentType)
    }

    fun primeStream(key: String, accumulatedRaw: String) {
        // Register stream position without speaking
        // Used when entering chat or with toggle off, so pre-existing content is not read aloud.
        streamKey = key
        streamCursor = accumulatedRaw.length
    }

    fun speakStream(key: String, accumulatedRaw: String, agentType: Agent.AgentType, complete: Boolean) {
        // Feed latest response's accumulated raw text; speaks only newly completed sentences past cursor.
        // Safe to call on every chunk update.
        if (key != streamKey) {          // a new response began
            stopSpeaking()
            streamKey = key              // (stopSpeaking cleared it)
            streamCursor = 0
        }
        val end = if (complete) accumulatedRaw.length else speakableBoundary(accumulatedRaw, streamCursor)
        if (end > streamCursor) {
            enqueue(accumulatedRaw.substring(streamCursor, end), agentType)
            streamCursor = end
        }
        // streamKey intentionally persists after completion: repeated effect runs
        // on the finished message hit cursor==length and no-op instead of restarting.
    }

    fun stopSpeaking() {
        speechWorker?.cancel()
        speechWorker = null
        speechQueue.cancel()
        speechQueue = Channel(Channel.UNLIMITED)
        streamKey = null
        streamCursor = 0
        playback.stop()
        _isSpeaking.value = false
    }

    private fun speakableBoundary(text: String, from: Int): Int {
        // Last sentence boundary at or before `text.length`, after `from`,
        // never crossing into an unclosed ``` fence. Returns `from` if none yet.
        var limit = text.length
        var idx = text.indexOf("```")
        var open = false
        while (idx >= 0) {
            open = !open
            limit = if (open) idx else text.length
            idx = text.indexOf("```", idx + 3)
        }
        val region = text.substring(0, limit)
        val hit = Regex("(?<=[.!?])(?<!\\d\\.)[\"')\\]]*\\s")
            .findAll(region)
            .lastOrNull { it.range.last >= from }
        return if (hit != null) hit.range.last + 1 else from
    }

    fun startListening() {
        if (!_modelHandler.isReady || _isListening.value) return
        stopSpeaking()  // never record while speaking
        capture.start()
        _isListening.value = true
    }

    suspend fun stopListening(): String {
        if (!_isListening.value) return ""
        _isListening.value = false
        val samples = capture.stop()
        val engine = ensureStt() ?: return ""
        if (samples.size < (STT_SAMPLE_RATE / 4)) return ""   // <250ms: accidental tap
        return withContext(Dispatchers.Default) {
            engine.transcribe(samples, STT_SAMPLE_RATE).trim()
        }
    }

    private fun chunkForSpeech(raw: String, minLen: Int = 24, maxLen: Int = 400): List<String> {
        // Markdown cleanup and sentence chunking
        val text = raw
            .replace(Regex("```[\\s\\S]*?```"), " Code block omitted. ")
            .replace(Regex("`([^`]*)`"), "$1")
            .replace(Regex("\\[([^\\]]+)]\\([^)]+\\)"), "$1")
            .replace(Regex("[*_#>|]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        if (text.isEmpty()) return emptyList()

        // Split after . ! ? (not decimals), optionally followed by quotes.
        val parts = text.split(Regex("(?<=[.!?])(?<!\\d\\.)[\"')\\]]*\\s+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        // Merge tiny fragments forward ("Aye." rides with the next sentence).
        val merged = mutableListOf<String>()
        for (part in parts) {
            if (merged.isNotEmpty() && merged.last().length < minLen) {
                merged[merged.lastIndex] = merged.last() + " " + part
            } else {
                merged.add(part)
            }
        }

        // Hard-split any monster chunk at commas/spaces.
        val out = mutableListOf<String>()
        for (chunk in merged) {
            var piece = chunk
            while (piece.length > maxLen) {
                var cut = piece.lastIndexOf(',', maxLen)
                if (cut < minLen) cut = piece.lastIndexOf(' ', maxLen)
                if (cut < minLen) cut = maxLen
                out.add(piece.substring(0, cut + 1).trim())
                piece = piece.substring(cut + 1).trim()
            }
            if (piece.isNotEmpty()) out.add(piece)
        }
        return out
    }

    fun startModelDownload() {
        _modelHandler.startDownload(
            address = preferences.serverAddress,
            port = preferences.serverPort,
            authKey = preferences.serverAuthToken,
            fingerprint = preferences.serverFingerprint
        )
    }

    fun checkForModelUpdates() {
        _modelHandler.checkForUpdates(
            address = preferences.serverAddress,
            port = preferences.serverPort,
            authKey = preferences.serverAuthToken,
            fingerprint = preferences.serverFingerprint
        )
    }

    private companion object {
        // kokoro-multi-lang-v1_0 speaker slots
        const val DAWSON_SID = 45          // blended voice's slot
        const val DAWSON_SPEED = 1.2f      // based on gravitas audition
        const val SQUIREBOT_SID = 16       // am_michael: flat, neutral base
        const val SQUIREBOT_SPEED = 1.2f
        const val STT_SAMPLE_RATE = 16_000
    }
}
