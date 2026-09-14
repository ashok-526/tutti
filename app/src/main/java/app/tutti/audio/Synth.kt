package app.tutti.audio

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

object Patch {
    const val CELLO = 0
    const val HARP = 1
    const val MARIMBA = 2
    const val FLUTE = 3
    const val CELESTA = 4
    const val BELL = 5
    const val PAD = 6
    const val BASS = 7
    const val KICK = 8
    const val HAT = 9
    const val SHAKER = 10
    const val CLICK = 11
}

/**
 * A small polyphonic synthesizer. Everything is computed sample by sample on the audio thread,
 * so all state lives in preallocated primitive arrays: nothing allocates while rendering.
 */
class Synth(private val sr: Int) {
    private companion object {
        const val VOICES = 48
        const val TABLE = 4096
        const val MASK = TABLE - 1
        val SINE = FloatArray(TABLE) { sin(2.0 * PI * it / TABLE).toFloat() }

        //                          CELLO  HARP   MARIM  FLUTE  CELES  BELL   PAD    BASS   KICK   HAT    SHAKE  CLICK
        val ATTACK = floatArrayOf(0.05f, 0.002f, 0.001f, 0.07f, 0.001f, 0.001f, 0.6f, 0.004f, 0.001f, 0.001f, 0.008f, 0.0005f)
        val DECAY = floatArrayOf(0.9f, 1.1f, 0.35f, 1.6f, 0.9f, 1.9f, 2.5f, 0.45f, 0.16f, 0.035f, 0.06f, 0.012f)
        val SUSTAIN = floatArrayOf(0.65f, 0f, 0f, 0.8f, 0f, 0f, 0.85f, 0.25f, 0f, 0f, 0f, 0f)
        val RELEASE = floatArrayOf(0.25f, 0.45f, 0.15f, 0.22f, 0.6f, 0.9f, 1.4f, 0.12f, 0.05f, 0.02f, 0.03f, 0.01f)
        val BRIGHT = floatArrayOf(0.3f, 0.09f, 0.05f, 0.5f, 0.12f, 0.5f, 0.5f, 0.5f, 0.03f, 0.5f, 0.5f, 0.5f)
        val GAIN = floatArrayOf(0.40f, 0.55f, 0.60f, 0.38f, 0.42f, 0.30f, 0.16f, 0.60f, 0.85f, 0.16f, 0.12f, 0.45f)
        val SEND = floatArrayOf(0.25f, 0.35f, 0.22f, 0.32f, 0.42f, 0.5f, 0.4f, 0.04f, 0.03f, 0.08f, 0.08f, 0f)
        val CUTOFF = floatArrayOf(1500f, 0f, 0f, 0f, 0f, 0f, 1000f, 0f, 0f, 0f, 0f, 0f)
    }

    private val active = BooleanArray(VOICES)
    private val patch = IntArray(VOICES)
    private val inc = DoubleArray(VOICES)
    private val ph = DoubleArray(VOICES)
    private val ph2 = DoubleArray(VOICES)
    private val age = IntArray(VOICES)
    private val gate = IntArray(VOICES)
    private val env = FloatArray(VOICES)
    private val env2 = FloatArray(VOICES)
    private val vel = FloatArray(VOICES)
    private val gainL = FloatArray(VOICES)
    private val gainR = FloatArray(VOICES)
    private val f1 = FloatArray(VOICES)
    private val f2 = FloatArray(VOICES)
    private val released = BooleanArray(VOICES)

    private val attackSamples = IntArray(12) { (ATTACK[it] * sr).toInt().coerceAtLeast(1) }
    private val decCoef = FloatArray(12) { exp(-1.0 / (DECAY[it] * sr)).toFloat() }
    private val relCoef = FloatArray(12) { exp(-1.0 / (RELEASE[it] * sr)).toFloat() }
    private val brightCoef = FloatArray(12) { exp(-1.0 / (BRIGHT[it] * sr)).toFloat() }
    private val cutCoef = FloatArray(12) { (1.0 - exp(-2.0 * PI * CUTOFF[it] / sr)).toFloat() }

    private var noise = 0x2545F491
    private var send = FloatArray(4096)
    private val reverb = Reverb(sr)

    fun note(patchId: Int, midi: Float, velocity: Float, gateSeconds: Float, pan: Float) {
        var v = -1
        for (i in 0 until VOICES) if (!active[i]) { v = i; break }
        if (v < 0) {
            var quietest = Float.MAX_VALUE
            for (i in 0 until VOICES) {
                val level = env[i] * vel[i]
                if (level < quietest) { quietest = level; v = i }
            }
        }
        active[v] = true
        patch[v] = patchId
        inc[v] = 440.0 * 2.0.pow((midi - 69.0) / 12.0) / sr
        ph[v] = 0.0
        ph2[v] = 0.0
        age[v] = 0
        gate[v] = (gateSeconds * sr).toInt()
        env[v] = 0f
        env2[v] = 1f
        vel[v] = velocity
        released[v] = false
        f1[v] = 0f
        f2[v] = 0f
        val angle = (pan.coerceIn(-1f, 1f) + 1f) * 0.25 * PI
        gainL[v] = cos(angle).toFloat()
        gainR[v] = sin(angle).toFloat()
    }

    fun releaseAll() {
        for (i in 0 until VOICES) if (active[i]) gate[i] = minOf(gate[i], age[i])
    }

    /** Renders [frames] frames into the two buffers (overwriting them). */
    fun render(outL: FloatArray, outR: FloatArray, frames: Int) {
        if (send.size < frames) send = FloatArray(frames)
        for (i in 0 until frames) { outL[i] = 0f; outR[i] = 0f; send[i] = 0f }

        for (v in 0 until VOICES) {
            if (!active[v]) continue
            val p = patch[v]
            val atk = attackSamples[p]
            val atkInc = 1f / atk
            val sus = SUSTAIN[p]
            val dec = decCoef[p]
            val rel = relCoef[p]
            val bright = brightCoef[p]
            val cut = cutCoef[p]
            val g = GAIN[p] * vel[v]
            val gl = gainL[v]
            val gr = gainR[v]
            val sendAmt = SEND[p]
            val dInc = inc[v]
            val gateAt = gate[v]
            var a = age[v]
            var e = env[v]
            var e2 = env2[v]
            var p1 = ph[v]
            var p2 = ph2[v]
            var s1 = f1[v]
            var s2 = f2[v]
            var isReleased = released[v]
            var alive = true
            var n = noise

            for (i in 0 until frames) {
                if (!isReleased && a >= gateAt) isReleased = true
                if (!isReleased && a < atk) {
                    e += atkInc
                    if (e > 1f) e = 1f
                } else if (!isReleased) {
                    e = sus + (e - sus) * dec
                } else {
                    e *= rel
                }
                e2 *= bright
                if (a > atk && (isReleased || sus == 0f) && e < 0.0004f) { alive = false; break }

                n = n xor (n shl 13); n = n xor (n ushr 17); n = n xor (n shl 5)
                val white = n * 4.656613e-10f

                val x: Float = when (p) {
                    Patch.CELLO -> {
                        val vib = if (a > sr / 4) 1.0 + 0.004 * SINE[((a * 5.5 / sr) * TABLE).toInt() and MASK] else 1.0
                        p1 += dInc * vib; if (p1 >= 1.0) p1 -= 1.0
                        val saw = (2.0 * p1 - 1.0).toFloat()
                        s1 += cut * (saw - s1); s2 += cut * (s1 - s2)
                        s2 * 1.6f
                    }
                    Patch.HARP -> {
                        p1 += dInc; if (p1 >= 1.0) p1 -= 1.0
                        val t = (p1 * TABLE).toInt()
                        SINE[t and MASK] + 0.32f * e2 * SINE[(t * 2) and MASK] + 0.1f * SINE[(t * 3) and MASK]
                    }
                    Patch.MARIMBA -> {
                        p1 += dInc; if (p1 >= 1.0) p1 -= 1.0
                        p2 += dInc * 3.93; if (p2 >= 1.0) p2 -= 1.0
                        SINE[(p1 * TABLE).toInt() and MASK] + 0.5f * e2 * SINE[(p2 * TABLE).toInt() and MASK]
                    }
                    Patch.FLUTE -> {
                        val vib = 1.0 + 0.006 * SINE[((a * 5.0 / sr) * TABLE).toInt() and MASK]
                        p1 += dInc * vib; if (p1 >= 1.0) p1 -= 1.0
                        val t = (p1 * TABLE).toInt()
                        s1 += 0.08f * (white - s1)
                        SINE[t and MASK] + 0.16f * SINE[(t * 2) and MASK] + 0.9f * s1
                    }
                    Patch.CELESTA -> {
                        p1 += dInc; if (p1 >= 1.0) p1 -= 1.0
                        p2 += dInc * 4.0; if (p2 >= 1.0) p2 -= 1.0
                        SINE[(p1 * TABLE).toInt() and MASK] + 0.38f * e2 * SINE[(p2 * TABLE).toInt() and MASK]
                    }
                    Patch.BELL -> {
                        p1 += dInc; if (p1 >= 1.0) p1 -= 1.0
                        p2 += dInc * 3.5; if (p2 >= 1.0) p2 -= 1.0
                        val mod = SINE[(p2 * TABLE).toInt() and MASK] * 0.38 * (0.3 + e2)
                        SINE[((p1 + mod) * TABLE).toInt() and MASK]
                    }
                    Patch.PAD -> {
                        p1 += dInc; if (p1 >= 1.0) p1 -= 1.0
                        p2 += dInc * 1.0065; if (p2 >= 1.0) p2 -= 1.0
                        val saw = (p1 + p2 - 1.0).toFloat()
                        s1 += cut * (saw - s1); s2 += cut * (s1 - s2)
                        s2 * 1.4f
                    }
                    Patch.BASS -> {
                        p1 += dInc; if (p1 >= 1.0) p1 -= 1.0
                        val t = (p1 * TABLE).toInt()
                        SINE[t and MASK] + 0.22f * SINE[(t * 2) and MASK]
                    }
                    Patch.KICK -> {
                        p1 += (46.0 + 120.0 * e2) / sr; if (p1 >= 1.0) p1 -= 1.0
                        SINE[(p1 * TABLE).toInt() and MASK]
                    }
                    Patch.HAT -> {
                        s1 += 0.6f * (white - s1)
                        white - s1
                    }
                    Patch.SHAKER -> {
                        s1 += 0.5f * (white - s1); s2 += 0.15f * (s1 - s2)
                        (s1 - s2) * 1.5f
                    }
                    else -> {
                        p1 += dInc; if (p1 >= 1.0) p1 -= 1.0
                        SINE[(p1 * TABLE).toInt() and MASK]
                    }
                }

                val s = x * e * g
                outL[i] += s * gl
                outR[i] += s * gr
                send[i] += s * sendAmt
                a++
            }

            noise = n
            if (!alive) { active[v] = false; continue }
            age[v] = a; env[v] = e; env2[v] = e2; ph[v] = p1; ph2[v] = p2
            f1[v] = s1; f2[v] = s2; released[v] = isReleased
        }

        reverb.process(send, outL, outR, frames)
        for (i in 0 until frames) {
            outL[i] = soften(outL[i] * 0.55f)
            outR[i] = soften(outR[i] * 0.55f)
        }
    }

    private fun soften(x: Float): Float = when {
        x > 1f -> 1f
        x < -1f -> -1f
        else -> x * (1.5f - 0.5f * x * x)
    }
}

/** Freeverb, trimmed down: four combs and two allpasses per side. */
private class Reverb(sr: Int) {
    private val scale = sr / 44100f
    private val combLens = intArrayOf(1116, 1188, 1277, 1356)
    private val combL = Array(4) { FloatArray((combLens[it] * scale).toInt()) }
    private val combR = Array(4) { FloatArray(((combLens[it] + 23) * scale).toInt()) }
    private val combIdxL = IntArray(4)
    private val combIdxR = IntArray(4)
    private val storeL = FloatArray(4)
    private val storeR = FloatArray(4)
    private val apL = arrayOf(FloatArray((556 * scale).toInt()), FloatArray((441 * scale).toInt()))
    private val apR = arrayOf(FloatArray((579 * scale).toInt()), FloatArray((464 * scale).toInt()))
    private val apIdxL = IntArray(2)
    private val apIdxR = IntArray(2)

    private val feedback = 0.8f
    private val damp = 0.3f

    fun process(input: FloatArray, outL: FloatArray, outR: FloatArray, frames: Int) {
        for (i in 0 until frames) {
            val x = input[i] * 0.3f
            var l = 0f
            var r = 0f
            for (c in 0 until 4) {
                val bl = combL[c]; val il = combIdxL[c]
                val ol = bl[il]
                storeL[c] = ol * (1f - damp) + storeL[c] * damp
                bl[il] = x + storeL[c] * feedback
                combIdxL[c] = if (il + 1 >= bl.size) 0 else il + 1
                l += ol

                val br = combR[c]; val ir = combIdxR[c]
                val or = br[ir]
                storeR[c] = or * (1f - damp) + storeR[c] * damp
                br[ir] = x + storeR[c] * feedback
                combIdxR[c] = if (ir + 1 >= br.size) 0 else ir + 1
                r += or
            }
            for (a in 0 until 2) {
                val bl = apL[a]; val il = apIdxL[a]
                val bo = bl[il]
                bl[il] = l + bo * 0.5f
                l = bo - l
                apIdxL[a] = if (il + 1 >= bl.size) 0 else il + 1

                val br = apR[a]; val ir = apIdxR[a]
                val bor = br[ir]
                br[ir] = r + bor * 0.5f
                r = bor - r
                apIdxR[a] = if (ir + 1 >= br.size) 0 else ir + 1
            }
            outL[i] += l
            outR[i] += r
        }
    }
}
