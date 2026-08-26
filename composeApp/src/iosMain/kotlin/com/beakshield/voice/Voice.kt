package com.beakshield.voice

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.get
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioFormat
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPCMFormatFloat32
import platform.AVFAudio.AVAudioPlayerNode
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.setActive
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite
import kotlin.coroutines.resume
import kotlin.math.min

// NOTE: Support for iOS TTS/STT is not complete and currently not working

@OptIn(ExperimentalForeignApi::class)
actual class TtsEngine actual constructor(modelDir: String) {
    // TODO(cinterop): construct SherpaOnnxOfflineTtsConfig with the kokoro
    // fields (model/voices/tokens/dataDir/lexicon under modelDir), then:
    //   private val tts = SherpaOnnxCreateOfflineTts(configPtr)
    private val modelDir = modelDir

    actual fun generate(text: String, sid: Int, speed: Float): TtsAudio {
        // TODO(cinterop):
        //   val audio = SherpaOnnxOfflineTtsGenerate(tts, text, sid, speed)
        //   val n = audio!!.pointed.n; val rate = audio.pointed.sample_rate
        //   copy audio.pointed.samples (CPointer<FloatVar>) into FloatArray
        //   SherpaOnnxDestroyOfflineTtsGeneratedAudio(audio)
        error("Wire sherpa-onnx cinterop (see VOICE_INTEGRATION.md §iOS)")
    }

    actual fun release() {
        // TODO(cinterop): SherpaOnnxDestroyOfflineTts(tts)
    }
}

@OptIn(ExperimentalForeignApi::class)
actual class SttEngine actual constructor(modelDir: String) {
    private val modelDir = modelDir

    actual fun transcribe(samples: FloatArray, sampleRate: Int): String {
        // TODO(cinterop):
        //   recognizer = SherpaOnnxCreateOfflineRecognizer(configPtr) [whisper]
        //   stream = SherpaOnnxCreateOfflineStream(recognizer)
        //   samples.usePinned { SherpaOnnxAcceptWaveformOffline(stream, rate, it.addressOf(0), samples.size) }
        //   SherpaOnnxDecodeOfflineStream(recognizer, stream)
        //   text = SherpaOnnxGetOfflineStreamResult(stream)!!.pointed.text!!.toKString()
        error("Wire sherpa-onnx cinterop (see VOICE_INTEGRATION.md §iOS)")
    }

    actual fun release() {}
}

// --- Audio I/O (AVFAudio) ----------------------------------------------------

@OptIn(ExperimentalForeignApi::class)
actual class AudioCapture actual constructor() {
    private val engine = AVAudioEngine()
    private val collected = mutableListOf<FloatArray>()

    actual fun start() {
        collected.clear()
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayAndRecord, error = null)
        session.setActive(true, error = null)

        val input = engine.inputNode
        val hwFormat = input.outputFormatForBus(0u)
        // Capture at hardware rate; downsample to 16k on stop (simple decimation
        // acceptable for speech; swap in AVAudioConverter for quality later).
        input.installTapOnBus(0u, bufferSize = 4096u, format = hwFormat) { buffer, _ ->
            val pcm = buffer ?: return@installTapOnBus
            val channel = pcm.floatChannelData?.get(0) ?: return@installTapOnBus
            val frames = pcm.frameLength.toInt()
            val copy = FloatArray(frames) { channel[it] }
            collected.add(copy)
        }
        engine.prepare()
        engine.startAndReturnError(null)
    }

    actual fun stop(): FloatArray {
        engine.inputNode.removeTapOnBus(0u)
        engine.stop()

        val hwRate = engine.inputNode.outputFormatForBus(0u).sampleRate
        val total = collected.sumOf { it.size }
        val all = FloatArray(total)
        var offset = 0
        for (b in collected) { b.copyInto(all, offset); offset += b.size }
        collected.clear()

        // Decimate hwRate -> 16000
        val ratio = hwRate / 16_000.0
        if (ratio <= 1.0) return all
        val outSize = (all.size / ratio).toInt()
        return FloatArray(outSize) { i -> all[min((i * ratio).toInt(), all.size - 1)] }
    }
}

@OptIn(ExperimentalForeignApi::class)
actual class AudioPlayback actual constructor() {
    private val engine = AVAudioEngine()
    private val player = AVAudioPlayerNode()
    private var attached = false

    actual suspend fun play(samples: FloatArray, sampleRate: Int) {
        val format = AVAudioFormat(AVAudioPCMFormatFloat32, sampleRate.toDouble(), 1u, false)
            ?: return
        if (!attached) {
            engine.attachNode(player)
            engine.connect(player, engine.mainMixerNode, format)
            attached = true
        }
        if (!engine.running) engine.startAndReturnError(null)

        val buffer = AVAudioPCMBuffer(format, samples.size.toUInt()) ?: return
        buffer.frameLength = samples.size.toUInt()
        val channel = buffer.floatChannelData?.get(0) ?: return
        for (i in samples.indices) channel[i] = samples[i]

        suspendCancellableCoroutine { cont ->
            player.scheduleBuffer(buffer) { if (cont.isActive) cont.resume(Unit) }
            cont.invokeOnCancellation { player.stop() }
            player.play()
        }
    }

    actual fun stop() {
        player.stop()
    }
}

// --- Files -------------------------------------------------------------------

actual fun voiceModelsDirectory(): String {
    val docs = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
        .firstOrNull() as? String ?: ""
    return "$docs/voice-models"
}

actual fun fileSize(path: String): Long? {
    val fm = NSFileManager.defaultManager
    if (!fm.fileExistsAtPath(path)) return null
    val attrs = fm.attributesOfItemAtPath(path, error = null) ?: return null
    return (attrs["NSFileSize"] as? Long)
}

actual fun ensureDirectory(path: String) {
    NSFileManager.defaultManager.createDirectoryAtPath(
        path, withIntermediateDirectories = true, attributes = null, error = null
    )
}

actual fun deleteFile(path: String) {
    NSFileManager.defaultManager.removeItemAtPath(path, error = null)
}

@OptIn(ExperimentalForeignApi::class)
actual class FileWriter actual constructor(path: String) {
    private val file = fopen(path, "wb")
    actual fun write(bytes: ByteArray, length: Int) {
        if (file == null || length <= 0) return
        bytes.usePinned { pinned ->
            fwrite(pinned.addressOf(0), 1u, length.toULong(), file)
        }
    }
    actual fun close() { if (file != null) fclose(file) }
}

@OptIn(ExperimentalForeignApi::class)
actual fun readTextFile(path: String): String? {
    return NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)
}

@OptIn(ExperimentalForeignApi::class)
actual fun writeTextFile(path: String, text: String) {
    (text as NSString).writeToFile(path, atomically = true, encoding = NSUTF8StringEncoding, error = null)
}
