package com.beakshield.voice

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt

object RobotVoiceEffect {
    // Used for Squirebots' voice
    private const val PITCH_SEMITONES = 1.5f
    private const val CHORUS_DEPTH_MS = 2.2f    // max timing offset — well under echo threshold
    private const val CHORUS_RATE_HZ = 0.9f     // slow drift, keeps it "synthetic" not "vibrato"
    private const val VOICE_A_MIX = 0.58f
    private const val VOICE_B_MIX = 0.42f
    private const val COMB_DELAY_SEC = 0.0009f
    private const val COMB_GAIN = 0.22f

    fun process(samples: FloatArray, sampleRate: Int): FloatArray {
        val base = resamplePitch(samples, PITCH_SEMITONES)

        // Second voice: same audio, timing wobbled ±CHORUS_DEPTH_MS.
        // Bounded offset = detuned-double sheen with no accumulating echo.
        val n = base.size
        val out = FloatArray(n)
        val depth = CHORUS_DEPTH_MS / 1000f * sampleRate
        val step = 2.0 * PI * CHORUS_RATE_HZ / sampleRate
        for (i in 0 until n) {
            val offset = depth * (0.5f + 0.5f * kotlin.math.sin(i * step).toFloat())
            val pos = i - offset
            val idx = pos.toInt().coerceIn(0, n - 2)
            val frac = (pos - idx).coerceIn(0f, 1f)
            val voiceB = base[idx] + frac * (base[idx + 1] - base[idx])
            out[i] = VOICE_A_MIX * base[i] + VOICE_B_MIX * voiceB
        }

        val delay = (COMB_DELAY_SEC * sampleRate).toInt().coerceAtLeast(1)
        for (i in delay until n) out[i] += COMB_GAIN * out[i - delay]

        matchLoudnessInPlace(out, samples)
        return out
    }

    private fun resamplePitch(samples: FloatArray, semitones: Float): FloatArray {
        if (abs(semitones) < 1e-6f || samples.size < 2) return samples.copyOf()
        val factor = 2.0.pow(semitones / 12.0)
        val outLength = ceil((samples.size - 1) / factor).toInt()
        val out = FloatArray(outLength)
        for (k in 0 until outLength) {
            val position = k * factor
            val index = position.toInt().coerceAtMost(samples.size - 2)
            val frac = (position - index).toFloat()
            out[k] = samples[index] + frac * (samples[index + 1] - samples[index])
        }
        return out
    }

    private fun matchLoudnessInPlace(samples: FloatArray, reference: FloatArray) {
        var refSum = 0.0; for (v in reference) refSum += v.toDouble() * v
        var procSum = 0.0; for (v in samples) procSum += v.toDouble() * v
        val refRms = sqrt(refSum / reference.size)
        val procRms = sqrt(procSum / samples.size)
        if (procRms < 1e-9) return
        var gain = (refRms / procRms).toFloat()
        var peak = 0f
        for (v in samples) { val x = abs(v * gain); if (x > peak) peak = x }
        if (peak > 0.95f) gain *= 0.95f / peak
        for (i in samples.indices) samples[i] *= gain
    }
}