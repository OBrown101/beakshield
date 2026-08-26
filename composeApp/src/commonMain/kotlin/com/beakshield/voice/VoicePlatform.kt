package com.beakshield.voice

// Platform seams for the voice feature. Everything else lives in commonMain.
// Android/JVM actuals wrap sherpa-onnx's Kotlin API (same package on both);
// iOS wraps the sherpa-onnx C API via cinterop plus AVFAudio for audio I/O.

/** Text-to-speech engine (sherpa-onnx OfflineTts, Kokoro model). */
expect class TtsEngine(modelDir: String) {
    /** Synthesize [text] with speaker [sid]; returns mono float32 samples. */
    fun generate(text: String, sid: Int, speed: Float): TtsAudio
    fun release()
}

data class TtsAudio(val samples: FloatArray, val sampleRate: Int)

/** Speech-to-text engine (sherpa-onnx OfflineRecognizer, Whisper model). */
expect class SttEngine(modelDir: String) {
    /** Transcribe 16kHz mono float32 samples. */
    fun transcribe(samples: FloatArray, sampleRate: Int): String
    fun release()
}

/** Push-to-talk microphone capture: 16kHz mono float32. */
expect class AudioCapture() {
    fun start()
    /** Stops capture and returns everything recorded since start(). */
    fun stop(): FloatArray
}

/** Sequential audio playback for synthesized speech. */
expect class AudioPlayback() {
    /** Plays [samples]; suspends until playback of this buffer completes. */
    suspend fun play(samples: FloatArray, sampleRate: Int)
    fun stop()
}

/** Absolute directory where voice model files are stored per platform. */
expect fun voiceModelsDirectory(): String

// Minimal file ops used by the model downloader.
expect fun fileSize(path: String): Long?
expect fun ensureDirectory(path: String)
expect fun deleteFile(path: String)
expect class FileWriter(path: String) {
    fun write(bytes: ByteArray, length: Int)
    fun close()
}
expect fun readTextFile(path: String): String?
expect fun writeTextFile(path: String, text: String)

expect fun fileSha256(path: String): String?
