package com.beakshield.voice

import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsKokoroModelConfig
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig
import com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.TargetDataLine

actual class TtsEngine actual constructor(modelDir: String) {
    private val tts = OfflineTts(
        OfflineTtsConfig.builder()
            .setModel(
                OfflineTtsModelConfig.builder()
                    .setKokoro(
                        OfflineTtsKokoroModelConfig.builder()
                            .setModel("$modelDir/model.onnx")
                            .setVoices("$modelDir/voices.bin")
                            .setTokens("$modelDir/tokens.txt")
                            .setDataDir("$modelDir/espeak-ng-data")
                            .setLexicon("$modelDir/lexicon-us-en.txt")
                            .build()
                    )
                    .setNumThreads(2)
                    .setProvider("cpu")
                    .build()
            )
            .build()
    )

    actual fun generate(text: String, sid: Int, speed: Float): TtsAudio {
        val audio = tts.generate(text, sid, speed)
        return TtsAudio(audio.samples, audio.sampleRate)
    }

    actual fun release() = tts.release()
}

actual class SttEngine actual constructor(modelDir: String) {
    private val recognizer = OfflineRecognizer(
        OfflineRecognizerConfig.builder()
            .setOfflineModelConfig(
                OfflineModelConfig.builder()
                    .setWhisper(
                        OfflineWhisperModelConfig.builder()
                            .setEncoder("$modelDir/tiny.en-encoder.int8.onnx")
                            .setDecoder("$modelDir/tiny.en-decoder.int8.onnx")
                            .build()
                    )
                    .setTokens("$modelDir/tokens.txt")
                    .setModelType("whisper")
                    .setNumThreads(2)
                    .build()
            )
            .setDecodingMethod("greedy_search")
            .build()
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
    private var line: TargetDataLine? = null
    private var thread: Thread? = null
    private val collected = ByteArrayOutputStream()

    actual fun start() {
        collected.reset()
        val format = AudioFormat(16_000f, 16, 1, true, false)   // 16k mono s16le
        val info = DataLine.Info(TargetDataLine::class.java, format)
        val dataLine = AudioSystem.getLine(info) as TargetDataLine
        dataLine.open(format)
        dataLine.start()
        line = dataLine
        thread = Thread {
            val buffer = ByteArray(3200)
            while (line != null) {
                val read = dataLine.read(buffer, 0, buffer.size)
                if (read > 0) synchronized(collected) { collected.write(buffer, 0, read) }
            }
        }.apply { start() }
    }

    actual fun stop(): FloatArray {
        val dataLine = line ?: return FloatArray(0)
        line = null
        thread?.join(500); thread = null
        runCatching { dataLine.stop(); dataLine.close() }
        val bytes = synchronized(collected) { collected.toByteArray() }
        // s16le -> float32
        val out = FloatArray(bytes.size / 2)
        for (i in out.indices) {
            val lo = bytes[2 * i].toInt() and 0xFF
            val hi = bytes[2 * i + 1].toInt()
            out[i] = ((hi shl 8) or lo).toShort().toInt() / 32768f
        }
        return out
    }
}

actual class AudioPlayback actual constructor() {
    @Volatile private var line: SourceDataLine? = null

    actual suspend fun play(samples: FloatArray, sampleRate: Int) = withContext(Dispatchers.IO) {
        stop()
        val format = AudioFormat(sampleRate.toFloat(), 16, 1, true, false)
        val info = DataLine.Info(SourceDataLine::class.java, format)
        val dataLine = AudioSystem.getLine(info) as SourceDataLine
        dataLine.open(format)
        dataLine.start()
        line = dataLine

        // float32 -> s16le
        val bytes = ByteArray(samples.size * 2)
        for (i in samples.indices) {
            val v = (samples[i].coerceIn(-1f, 1f) * 32767f).toInt()
            bytes[2 * i] = (v and 0xFF).toByte()
            bytes[2 * i + 1] = ((v shr 8) and 0xFF).toByte()
        }
        dataLine.write(bytes, 0, bytes.size)   // blocks as the buffer drains
        if (line === dataLine) {
            dataLine.drain()
            dataLine.close()
            line = null
        }
    }

    actual fun stop() {
        line?.let { runCatching { it.stop(); it.flush(); it.close() } }
        line = null
    }
}

actual fun voiceModelsDirectory(): String =
    File(System.getProperty("user.home"), ".beakshield/voice-models").absolutePath

actual fun fileSize(path: String): Long? = File(path).takeIf { it.isFile }?.length()
actual fun ensureDirectory(path: String) { File(path).mkdirs() }
actual fun deleteFile(path: String) { File(path).delete() }

actual class FileWriter actual constructor(path: String) {
    private val stream = FileOutputStream(path.also { File(it).parentFile?.mkdirs() })
    actual fun write(bytes: ByteArray, length: Int) = stream.write(bytes, 0, length)
    actual fun close() = stream.close()
}

actual fun readTextFile(path: String): String? = File(path).takeIf { it.isFile }?.readText()

actual fun writeTextFile(path: String, text: String) = File(path).apply { parentFile?.mkdirs() }.writeText(text)

actual fun fileSha256(path: String): String? {
    val file = File(path).takeIf { it.isFile } ?: return null
    val md = MessageDigest.getInstance("SHA-256")
    file.inputStream().use { input ->
        val buf = ByteArray(64 * 1024)
        while (true) {
            val n = input.read(buf); if (n < 0) break
            md.update(buf, 0, n)
        }
    }
    return md.digest().joinToString("") { "%02x".format(it) }
}