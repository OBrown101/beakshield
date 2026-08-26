package com.beakshield.voice

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsKokoroModelConfig
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

// NOTE: Support for Android not complete, still need to add following to MainActivity and test
//          VoiceAndroid.appContext = applicationContext

object VoiceAndroid {
    lateinit var appContext: Context
}

actual class TtsEngine actual constructor(modelDir: String) {
    private val tts = OfflineTts(
        config = OfflineTtsConfig(
            model = OfflineTtsModelConfig(
                kokoro = OfflineTtsKokoroModelConfig(
                    model = "$modelDir/model.onnx",
                    voices = "$modelDir/voices.bin",
                    tokens = "$modelDir/tokens.txt",
                    dataDir = "$modelDir/espeak-ng-data",
                    lexicon = "$modelDir/lexicon-us-en.txt"   // VERIFY param name
                ),
                numThreads = 2,
                provider = "cpu"
            )
        )
    )

    actual fun generate(text: String, sid: Int, speed: Float): TtsAudio {
        val audio = tts.generate(text = text, sid = sid, speed = speed)
        return TtsAudio(audio.samples, audio.sampleRate)
    }

    actual fun release() = tts.release()
}

actual class SttEngine actual constructor(modelDir: String) {
    private val recognizer = OfflineRecognizer(
        config = OfflineRecognizerConfig(
            modelConfig = OfflineModelConfig(
                whisper = OfflineWhisperModelConfig(
                    encoder = "$modelDir/tiny.en-encoder.int8.onnx",
                    decoder = "$modelDir/tiny.en-decoder.int8.onnx"
                ),
                tokens = "$modelDir/tokens.txt",
                modelType = "whisper",
                numThreads = 2
            )
        )
    )

    actual fun transcribe(samples: FloatArray, sampleRate: Int): String {
        val stream = recognizer.createStream()
        stream.acceptWaveform(samples, sampleRate)
        recognizer.decode(stream)
        val text = recognizer.getResult(stream).text
        stream.release()
        return text
    }

    actual fun release() = recognizer.release()
}

actual class AudioCapture actual constructor() {
    private var recorder: AudioRecord? = null
    private var thread: Thread? = null
    private val buffers = mutableListOf<FloatArray>()

    @SuppressLint("MissingPermission")   // RECORD_AUDIO requested by UI first
    actual fun start() {
        buffers.clear()
        val minBuffer = AudioRecord.getMinBufferSize(
            16_000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT
        )
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            16_000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT,
            maxOf(minBuffer, 16_000)
        )
        recorder = audioRecord
        audioRecord.startRecording()
        thread = Thread {
            val chunk = FloatArray(1600)
            while (recorder != null) {
                val read = audioRecord.read(chunk, 0, chunk.size, AudioRecord.READ_BLOCKING)
                if (read > 0) synchronized(buffers) { buffers.add(chunk.copyOf(read)) }
            }
        }.apply { start() }
    }

    actual fun stop(): FloatArray {
        val audioRecord = recorder ?: return FloatArray(0)
        recorder = null
        thread?.join(500); thread = null
        runCatching { audioRecord.stop(); audioRecord.release() }
        synchronized(buffers) {
            val total = buffers.sumOf { it.size }
            val out = FloatArray(total)
            var offset = 0
            for (b in buffers) { b.copyInto(out, offset); offset += b.size }
            buffers.clear()
            return out
        }
    }
}

actual class AudioPlayback actual constructor() {
    private var track: AudioTrack? = null

    actual suspend fun play(samples: FloatArray, sampleRate: Int) {
        stop()
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(samples.size * 4)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track = audioTrack
        audioTrack.write(samples, 0, samples.size, AudioTrack.WRITE_BLOCKING)

        suspendCancellableCoroutine { cont ->
            audioTrack.setNotificationMarkerPosition(samples.size)
            audioTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(t: AudioTrack?) { if (cont.isActive) cont.resume(Unit) }
                override fun onPeriodicNotification(t: AudioTrack?) {}
            })
            cont.invokeOnCancellation { runCatching { audioTrack.stop() } }
            audioTrack.play()
        }
    }

    actual fun stop() {
        track?.let { runCatching { it.stop(); it.release() } }
        track = null
    }
}

actual fun voiceModelsDirectory(): String =
    File(VoiceAndroid.appContext.filesDir, "voice-models").absolutePath

actual fun fileSize(path: String): Long? = File(path).takeIf { it.isFile }?.length()
actual fun ensureDirectory(path: String) { File(path).mkdirs() }
actual fun deleteFile(path: String) { File(path).delete() }

actual class FileWriter actual constructor(path: String) {
    private val stream = FileOutputStream(path.also { File(it).parentFile?.mkdirs() })
    actual fun write(bytes: ByteArray, length: Int) = stream.write(bytes, 0, length)
    actual fun close() = stream.close()
}

actual fun readTextFile(path: String): String? =
    File(path).takeIf { it.isFile }?.readText()

actual fun writeTextFile(path: String, text: String) {
    File(path).apply { parentFile?.mkdirs() }.writeText(text)
}
