package com.beakshield.voice

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tanh

object GravitasVoiceEffect {
    // Used to get Dawson's specific voice
    private const val PITCH_SEMITONES = -2.0f   // depth (resample-based)
    private const val DRIVE = 2.0f              // rasp / harmonic grain
    private const val GROWL_DEPTH = 0.18f       // throat-resonance flutter
    private const val GROWL_HZ = 75.0f
    private const val LOWPASS_HZ = 7000.0f      // darkness shelf

    /** 2^(|PITCH_SEMITONES|/12): multiply desired pacing by this to get
     *  the TTS speed (or equivalently: DAWSON_SPEED = audition --tts-speed). */
    const val PITCH_STRETCH = 1.12246f

    fun process(samples: FloatArray, sampleRate: Int): FloatArray {
        var out = pitchShift(samples, PITCH_SEMITONES)
        out = saturate(out, DRIVE)
        growlInPlace(out, sampleRate, GROWL_DEPTH, GROWL_HZ)
        lowpassInPlace(out, sampleRate, LOWPASS_HZ)
        matchLoudnessInPlace(out, reference = samples)
        return out
    }

    private fun pitchShift(samples: FloatArray, semitones: Float): FloatArray {
        // Resample-based shift (mirrors np.interp over positions k*factor).
        // Negative semitones lower pitch and lengthen the audio.
        if (abs(semitones) < 1e-6f || samples.size < 2) return samples.copyOf()
        val factor = 2.0.pow(semitones / 12.0)
        val outLength = ceil((samples.size - 1) / factor).toInt()
        val out = FloatArray(outLength)
        for (k in 0 until outLength) {
            val position = k * factor
            val index = position.toInt()
            val frac = (position - index).toFloat()
            out[k] = samples[index] + frac * (samples[index + 1] - samples[index])
        }
        return out
    }

    private fun saturate(samples: FloatArray, drive: Float): FloatArray {
        // tanh waveshaping: compresses peaks, adds odd harmonics — the grain.
        if (drive <= 1.0f) return samples
        val norm = tanh(drive.toDouble())
        return FloatArray(samples.size) { i ->
            (tanh(samples[i] * drive.toDouble()) / norm).toFloat()
        }
    }

    private fun growlInPlace(samples: FloatArray, sampleRate: Int, depth: Float, hz: Float) {
        // Low-frequency amplitude modulation — the throat-resonance flutter.
        if (depth <= 0.0f) return
        val step = 2.0 * PI * hz / sampleRate
        val d = depth.toDouble()
        for (i in samples.indices) {
            val modulator = (1.0 - d) + d * 0.5 * (1.0 + sin(i * step))
            samples[i] = (samples[i] * modulator).toFloat()
        }
    }

    private fun lowpassInPlace(samples: FloatArray, sampleRate: Int, cutoffHz: Float) {
        // One-pole low-pass: rolls off the airy top end for darkness.
        if (cutoffHz <= 0f || cutoffHz >= sampleRate / 2f) return
        val alpha = 1.0 - exp(-2.0 * PI * cutoffHz / sampleRate)
        var acc = 0.0
        for (i in samples.indices) {
            acc += alpha * (samples[i] - acc)
            samples[i] = acc.toFloat()
        }
    }

    private fun matchLoudnessInPlace(samples: FloatArray, reference: FloatArray) {
        // RMS-match to the pre-effect audio; guard peaks at 0.95.
        var refSum = 0.0
        for (v in reference) refSum += v.toDouble() * v
        var procSum = 0.0
        for (v in samples) procSum += v.toDouble() * v
        val refRms = sqrt(refSum / reference.size)
        val procRms = sqrt(procSum / samples.size)
        if (procRms < 1e-9) return

        var gain = (refRms / procRms).toFloat()
        var peak = 0f
        for (v in samples) { val a = abs(v * gain); if (a > peak) peak = a }
        if (peak > 0.95f) gain *= 0.95f / peak

        for (i in samples.indices) samples[i] *= gain
    }
}
