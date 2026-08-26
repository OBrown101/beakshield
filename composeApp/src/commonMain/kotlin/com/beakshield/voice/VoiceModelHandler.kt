package com.beakshield.voice

import com.beakshield.dawsonHttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class VoiceModelHandler {
    private val json = Json { ignoreUnknownKeys = true }
    private val scope = CoroutineScope(Dispatchers.Default)
    private var downloadJob: Job? = null

    sealed interface State {
        data object NotDownloaded : State
        data class Downloading(val currentFile: String, val doneBytes: Long, val totalBytes: Long) : State {
            val progress: Float
                get() = if (totalBytes > 0) (doneBytes.toFloat() / totalBytes) else 0f
        }
        data object Ready : State
        data class Error(val message: String) : State
    }

    private val _state = MutableStateFlow<State>(notDownloadedOrReady())
    val state = _state.asStateFlow()

    val isReady: Boolean
        get() = _state.value is State.Ready

    @Serializable
    data class Manifest(val version: Int, val files: List<Entry>) {
        @Serializable
        data class Entry(val path: String, val bytes: Long, val sha256: String)
    }

    private fun notDownloadedOrReady(): State {
        val cached = readTextFile(cachedManifestPath()) ?: return State.NotDownloaded
        val manifest = runCatching {
            json.decodeFromString<Manifest>(cached)
        }.getOrNull() ?: return State.NotDownloaded
        val complete = manifest.files.all { fileSize(localPath(it.path)) == it.bytes }
        return if (complete && manifest.files.isNotEmpty()) State.Ready else State.NotDownloaded
    }

    fun startDownload(address: String, port: Int, authKey: String, fingerprint: String) {
        if (downloadJob?.isActive == true) return
        downloadJob = scope.launch {
            try {
                val client = dawsonHttpClient(fingerprint)
                val base = "https://$address:$port/voice-models"

                val manifestText = client.get("$base/manifest") { bearerAuth(authKey) }.bodyAsText()
                val manifest = json.decodeFromString<Manifest>(manifestText)
                if (manifest.files.isEmpty()) {
                    _state.value = State.Error("The server's voice-models folder is empty. Place the Kokoro and Whisper model files there first.")
                    return@launch
                }

                val totalBytes = manifest.files.sumOf { it.bytes }
                var doneBytes = 0L

                for (entry in manifest.files) {
                    val destination = localPath(entry.path)

                    // Skip files already fully present (resume after interruption).
                    if (entryValid(entry)) {
                        doneBytes += entry.bytes
                        _state.value = State.Downloading(entry.path, doneBytes, totalBytes)
                        continue
                    }

                    _state.value = State.Downloading(entry.path, doneBytes, totalBytes)
                    ensureDirectory(destination.substringBeforeLast('/'))
                    deleteFile(destination)   // never resume a partial file mid-way

                    val response = client.get("$base/file/${entry.path}") { bearerAuth(authKey) }
                    if (!response.status.isSuccess()) {
                        _state.value = State.Error("Download failed for ${entry.path}: HTTP ${response.status.value}")
                        return@launch
                    }

                    val channel = response.bodyAsChannel()
                    val writer = FileWriter(destination)
                    try {
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            val read = channel.readAvailable(buffer, 0, buffer.size)
                            if (read <= 0) break
                            writer.write(buffer, read)
                            doneBytes += read
                            _state.value = State.Downloading(entry.path, doneBytes, totalBytes)
                        }
                    } finally {
                        writer.close()
                    }

                    if (!entryValid(entry)) {
                        _state.value = State.Error("Verification failed for ${entry.path}; download interrupted or corrupted. Tap enable again to resume.")
                        return@launch
                    }
                }

                writeTextFile(cachedManifestPath(), manifestText)
                _state.value = State.Ready
            } catch (e: Exception) {
                _state.value = State.Error(e.message ?: "Voice model download failed")
            }
        }
    }

    fun checkForUpdates(address: String, port: Int, authKey: String, fingerprint: String) {
        if (downloadJob?.isActive == true || _state.value !is State.Ready) return
        scope.launch {
            runCatching {
                val client = dawsonHttpClient(fingerprint)
                val manifestText = client.get("https://$address:$port/voice-models/manifest") {
                    bearerAuth(authKey)
                }.bodyAsText()
                if (manifestText != readTextFile(cachedManifestPath())) {
                    startDownload(address, port, authKey, fingerprint)
                }
            }
        }
    }

    fun localPath(relative: String): String {
        return "${voiceModelsDirectory()}/$relative"
    }
    private fun cachedManifestPath(): String {
        return "${voiceModelsDirectory()}/manifest.cached.json"
    }

    private fun entryValid(entry: Manifest.Entry): Boolean {
        if (fileSize(localPath(entry.path)) != entry.bytes) return false
        return (fileSha256(localPath(entry.path)) == entry.sha256)
    }
}
