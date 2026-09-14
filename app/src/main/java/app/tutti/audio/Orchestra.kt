package app.tutti.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Process
import app.tutti.model.Action
import app.tutti.model.Instrument
import app.tutti.model.Recipe
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.ceil
import kotlin.math.min

/**
 * The live score. The UI thread describes what every dish is doing; the audio thread turns that
 * into music on a 16th-note grid in D major. Tempo follows the cook's hands, each dish plays its
 * own instrument, and a dish's leitmotif announces every moment it needs attention.
 */
class Orchestra {
    companion object {
        const val SR = 48_000
        const val SILENT = 0
        const val ACTIVE = 1
        const val COOKING = 2
        const val BUILD = 3
        const val READY = 4

        private const val MAX_DISHES = 8
        private const val EV_COUNT_IN = 1
        private const val EV_TUTTI = 2
        private const val EV_ENCORE = 3
        private const val EV_CUE = 100

        private val SCALE = intArrayOf(0, 2, 4, 5, 7, 9, 11)
        private val PENTA = intArrayOf(0, 2, 4, 7, 9)
        private val PROGRESSION = intArrayOf(0, 5, 3, 4)

        fun patchFor(instrument: Instrument) = when (instrument) {
            Instrument.CELLO -> Patch.CELLO
            Instrument.HARP -> Patch.HARP
            Instrument.MARIMBA -> Patch.MARIMBA
            Instrument.FLUTE -> Patch.FLUTE
            Instrument.CELESTA -> Patch.CELESTA
        }
    }

    private class Voice(val patch: Int, val base: Int, val pan: Float, val motif: IntArray)

    @Volatile private var voices: Array<Voice> = emptyArray()
    private val mode = IntArray(MAX_DISHES)
    private val action = IntArray(MAX_DISHES)
    private val build = FloatArray(MAX_DISHES)
    private val events = ConcurrentLinkedQueue<Int>()

    @Volatile var targetBpm = 84f
    @Volatile var layersOn = false
    @Volatile var muted = false

    /** Beats elapsed, for the UI to pulse in time. */
    @Volatile var beatClock = 0.0
        private set
    @Volatile var bpm = 84f
        private set

    @Volatile private var running = false
    private var thread: Thread? = null

    @Volatile private var recordFile: File? = null
    @Volatile private var stopRecording = false

    fun configure(recipes: List<Recipe>) {
        voices = recipes.map {
            Voice(patchFor(it.instrument), it.instrument.baseMidi, it.instrument.pan, it.motif.toIntArray())
        }.toTypedArray()
        for (d in 0 until MAX_DISHES) { mode[d] = SILENT; build[d] = 0f }
    }

    fun setDish(dish: Int, dishMode: Int, dishAction: Action, buildLevel: Float = 0f) {
        if (dish >= MAX_DISHES) return
        action[dish] = dishAction.ordinal
        build[dish] = buildLevel
        mode[dish] = dishMode
    }

    fun cue(dish: Int) { events.add(EV_CUE + dish) }
    fun countIn() { events.add(EV_COUNT_IN) }
    fun tutti() { events.add(EV_TUTTI) }
    fun encore() { events.add(EV_ENCORE) }

    fun record(file: File) { stopRecording = false; recordFile = file }
    fun endRecording() { stopRecording = true }

    fun start() {
        if (running) return
        running = true
        thread = Thread({ loop() }, "tutti-orchestra").also { it.start() }
    }

    fun stop() {
        running = false
        thread?.join(500)
        thread = null
    }

    // ---- Audio thread ---------------------------------------------------------------------

    private val synth = Synth(SR)
    private var tick = 0L
    private var samplesToTick = 0.0
    private var chordDeg = 0
    private var tonicBars = 0
    private var countIn = 0
    private var finaleTick = -1
    private var encoreTick = -1
    private val motifTick = IntArray(MAX_DISHES) { -1 }
    private val motifVel = FloatArray(MAX_DISHES)
    private var rng = 0x6D2B79F5
    private var gain = 1f

    private fun loop() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
        val frames = 480
        val track = try {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                        .setSampleRate(SR)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(frames * 2 * 4 * 4)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build().also { it.play() }
        } catch (t: Throwable) {
            null
        }

        val left = FloatArray(frames)
        val right = FloatArray(frames)
        val chunkL = FloatArray(frames)
        val chunkR = FloatArray(frames)
        val interleaved = FloatArray(frames * 2)
        val pcm = ByteArray(frames * 4)
        var recorder: RandomAccessFile? = null
        var recorded = 0L
        val startNs = System.nanoTime()
        var written = 0L
        // Sample 0 of any recording lines up with this moment, not with when start() was called.
        android.util.Log.i("Tutti", "audio-start ${System.currentTimeMillis()}")

        while (running) {
            var pos = 0
            while (pos < frames) {
                if (samplesToTick <= 0.0) {
                    onTick()
                    samplesToTick += SR * 60.0 / bpm / 4.0
                }
                val chunk = min(frames - pos, ceil(samplesToTick).toInt().coerceAtLeast(1))
                synth.render(chunkL, chunkR, chunk)
                System.arraycopy(chunkL, 0, left, pos, chunk)
                System.arraycopy(chunkR, 0, right, pos, chunk)
                samplesToTick -= chunk
                pos += chunk
            }
            val samplesPerTick = SR * 60.0 / bpm / 4.0
            beatClock = (tick + (1.0 - samplesToTick / samplesPerTick)) / 4.0

            val target = if (muted) 0f else 1f
            for (i in 0 until frames) {
                gain += (target - gain) * 0.002f
                interleaved[i * 2] = left[i] * gain
                interleaved[i * 2 + 1] = right[i] * gain
            }

            recordFile?.let { file ->
                if (recorder == null) {
                    recorder = RandomAccessFile(file, "rw").also { it.setLength(0); it.write(ByteArray(44)) }
                    recorded = 0
                }
                for (i in 0 until frames * 2) {
                    val s = (interleaved[i].coerceIn(-1f, 1f) * 32767).toInt()
                    pcm[i * 2] = s.toByte()
                    pcm[i * 2 + 1] = (s shr 8).toByte()
                }
                recorder?.write(pcm, 0, frames * 4)
                recorded += frames * 4
            }
            if (stopRecording && recorder != null) {
                finishWav(recorder!!, recorded)
                recorder = null
                recordFile = null
                stopRecording = false
            }

            track?.write(interleaved, 0, frames * 2, AudioTrack.WRITE_BLOCKING)
            written += frames
            // Keep real time even when the audio sink doesn't block (e.g. a silent emulator).
            val aheadMs = written * 1000 / SR - (System.nanoTime() - startNs) / 1_000_000 - 80
            if (aheadMs > 0) Thread.sleep(aheadMs)
        }

        recorder?.let { finishWav(it, recorded) }
        track?.run { pause(); flush(); release() }
    }

    private fun finishWav(file: RandomAccessFile, dataBytes: Long) {
        fun le32(v: Long) = byteArrayOf(v.toByte(), (v shr 8).toByte(), (v shr 16).toByte(), (v shr 24).toByte())
        fun le16(v: Int) = byteArrayOf(v.toByte(), (v shr 8).toByte())
        file.seek(0)
        file.write("RIFF".toByteArray()); file.write(le32(36 + dataBytes)); file.write("WAVE".toByteArray())
        file.write("fmt ".toByteArray()); file.write(le32(16)); file.write(le16(1)); file.write(le16(2))
        file.write(le32(SR.toLong())); file.write(le32(SR * 4L)); file.write(le16(4)); file.write(le16(16))
        file.write("data".toByteArray()); file.write(le32(dataBytes))
        file.close()
    }

    private fun random(): Float {
        rng = rng xor (rng shl 13); rng = rng xor (rng ushr 17); rng = rng xor (rng shl 5)
        return (rng ushr 1) / Int.MAX_VALUE.toFloat()
    }

    private fun chordTone(deg: Int, k: Int, base: Int): Float {
        val d = deg + k * 2
        return (base + SCALE[d % 7] + 12 * (d / 7)).toFloat()
    }

    private fun penta(i: Int, base: Int): Float = (base + PENTA[i % 5] + 12 * (i / 5)).toFloat()

    private fun onTick() {
        while (true) {
            val ev = events.poll() ?: break
            when {
                ev == EV_COUNT_IN -> { tick = 0; countIn = 16; bpm = targetBpm }
                ev == EV_TUTTI -> { finaleTick = 0; encoreTick = -1 }
                ev == EV_ENCORE -> { encoreTick = 0; finaleTick = -1; tick = 0 }
                ev >= EV_CUE -> {
                    val d = ev - EV_CUE
                    if (d < voices.size) {
                        motifTick[d] = 0; motifVel[d] = 0.75f; tonicBars = 1
                        synth.note(Patch.BELL, 86f, 0.5f, 0.1f, 0f)
                        synth.note(Patch.BELL, 93f, 0.35f, 0.1f, 0.2f)
                    }
                }
            }
        }

        val step = (tick % 16).toInt()
        val bar = (tick / 16).toInt()
        val v = voices

        if (step == 0) {
            bpm = targetBpm
            val tension = (v.indices).any { mode[it] == BUILD && build[it] > 0.6f }
            chordDeg = when {
                tonicBars > 0 -> { tonicBars--; 0 }
                tension -> 4
                else -> PROGRESSION[bar % 4]
            }
        }

        if (countIn > 0) {
            if (countIn % 4 == 0) synth.note(Patch.CLICK, if (countIn == 16) 96f else 89f, 0.8f, 0.02f, 0f)
            countIn--
        }

        if (layersOn && countIn == 0 && finaleTick < 0 && encoreTick < 0) {
            playLayers(v, step, bar)
        }
        if (finaleTick >= 0) playFinale(v)
        if (encoreTick >= 0) playEncore(v)

        for (d in v.indices) {
            val t = motifTick[d]
            if (t < 0) continue
            if (t % 2 == 0 && t / 2 < v[d].motif.size) {
                val last = t / 2 == v[d].motif.size - 1
                synth.note(v[d].patch, penta(v[d].motif[t / 2], v[d].base), motifVel[d], if (last) 0.7f else 0.18f, v[d].pan)
            }
            motifTick[d] = if (t >= v[d].motif.size * 2 + 2) -1 else t + 1
        }
        tick++
    }

    private fun playLayers(v: Array<Voice>, step: Int, bar: Int) {
        val beatSec = 60f / bpm
        if (step == 0 || step == 8) synth.note(Patch.BASS, chordTone(chordDeg, 0, 38), 0.55f, beatSec * 1.5f, 0f)
        if (step == 0) for (k in 0..2) synth.note(Patch.PAD, chordTone(chordDeg, k, 57), 0.55f, beatSec * 3.8f, (k - 1) * 0.4f)

        val anyActive = v.indices.any { mode[it] == ACTIVE }
        if (anyActive) {
            if (step % 4 == 0) synth.note(Patch.KICK, 36f, 0.55f, 0.1f, 0f)
            if (step % 4 == 2) synth.note(Patch.HAT, 90f, 0.45f, 0.03f, 0.15f)
        }

        for (d in v.indices) {
            val voice = v[d]
            when (mode[d]) {
                ACTIVE -> ostinato(voice, Action.entries[action[d]], step, beatSec)
                COOKING -> cooking(voice, Action.entries[action[d]], step, bar, beatSec, 1.6f)
                BUILD -> {
                    cooking(voice, Action.entries[action[d]], step, bar, beatSec, 1f)
                    if (step == 0 && motifTick[d] < 0) {
                        motifTick[d] = 0
                        motifVel[d] = 0.18f + 0.4f * build[d]
                    }
                }
                READY -> if (step == 0 && bar % 2 == 0) {
                    synth.note(voice.patch, penta(5, voice.base), 0.12f, beatSec * 3f, voice.pan)
                }
            }
        }
    }

    private fun ostinato(voice: Voice, act: Action, step: Int, beatSec: Float) {
        val base = voice.base
        when (act) {
            Action.CHOP -> if (step % 2 == 0) {
                synth.note(voice.patch, chordTone(chordDeg, (step / 2) % 3, base), 0.5f, 0.09f, voice.pan)
            }
            Action.WHISK -> {
                synth.note(voice.patch, chordTone(chordDeg, step % 2 + 1, base), 0.3f, 0.05f, voice.pan)
                synth.note(Patch.SHAKER, 90f, if (step % 2 == 0) 0.55f else 0.3f, 0.03f, -0.2f)
            }
            Action.STIR -> if (step % 2 == 0) {
                val shape = intArrayOf(0, 1, 2, 3, 2, 1, 2, 1)
                synth.note(voice.patch, chordTone(chordDeg, shape[step / 2], base), 0.42f, beatSec * 0.6f, voice.pan)
            }
            Action.SEAR -> {
                val hits = intArrayOf(0, 3, 6, 8, 11, 14)
                val tones = intArrayOf(0, 2, 1, 0, 2, 3)
                val i = hits.indexOf(step)
                if (i >= 0) synth.note(voice.patch, chordTone(chordDeg, tones[i], base), 0.5f, 0.12f, voice.pan)
                synth.note(Patch.HAT, 90f, 0.22f, 0.02f, 0.3f)
            }
            Action.PLATE -> if (step % 8 == 0) {
                for (k in 0..2) synth.note(voice.patch, chordTone(chordDeg, k, base), 0.35f, beatSec * 1.8f, voice.pan)
            }
            else -> if (step % 4 == 0) {
                synth.note(voice.patch, chordTone(chordDeg, (step / 4) % 3, base), 0.45f, beatSec * 0.5f, voice.pan)
            }
        }
    }

    private fun cooking(voice: Voice, act: Action, step: Int, bar: Int, beatSec: Float, level: Float) {
        val low = if (voice.base >= 62) voice.base - 12 else voice.base
        when (act) {
            Action.BOIL -> if (step % 2 == 1 && random() < 0.3f) {
                synth.note(voice.patch, penta(5 + (random() * 5).toInt(), voice.base), 0.14f * level, 0.05f, voice.pan)
            }
            Action.SEAR -> {
                if (step % 2 == 1) synth.note(Patch.HAT, 90f, 0.14f * level, 0.02f, voice.pan)
                if (step == 0) synth.note(voice.patch, chordTone(chordDeg, 0, low), 0.16f * level, beatSec * 3f, voice.pan)
            }
            Action.BAKE -> if (step == 0 && bar % 2 == 0) {
                synth.note(voice.patch, chordTone(chordDeg, 2, low), 0.16f * level, beatSec * 7f, voice.pan)
            }
            Action.REST -> if (step == 0 && bar % 2 == 1) {
                synth.note(voice.patch, penta(voice.motif[0], voice.base), 0.1f * level, beatSec * 2f, voice.pan)
            }
            Action.HEAT -> if (step == 0) {
                synth.note(voice.patch, chordTone(chordDeg, 0, low), 0.14f * level, beatSec * 3.5f, voice.pan)
            }
            else -> {
                if (step == 0) synth.note(voice.patch, chordTone(chordDeg, 2, low), 0.15f * level, beatSec * 3.5f, voice.pan)
                if (step == 10 && random() < 0.5f) {
                    synth.note(voice.patch, penta(6 + (random() * 3).toInt(), voice.base), 0.08f * level, 0.05f, voice.pan)
                }
            }
        }
    }

    private fun bigChord(v: Array<Voice>, beatSec: Float) {
        synth.note(Patch.KICK, 36f, 0.8f, 0.2f, 0f)
        synth.note(Patch.BASS, 38f, 0.6f, beatSec * 8f, 0f)
        for (k in 0..3) synth.note(Patch.PAD, chordTone(0, k, 57), 0.6f, beatSec * 8f, (k - 1.5f) * 0.4f)
        val each = 0.75f / v.size.coerceAtLeast(1)
        for (voice in v) for (k in 0..2) {
            synth.note(voice.patch, chordTone(0, k, voice.base), each, beatSec * 6f, voice.pan)
        }
        synth.note(Patch.BELL, 86f, 0.35f, 0.2f, -0.3f)
        synth.note(Patch.BELL, 90f, 0.3f, 0.2f, 0.3f)
        synth.note(Patch.BELL, 93f, 0.25f, 0.2f, 0f)
    }

    private fun playFinale(v: Array<Voice>) {
        val beatSec = 60f / bpm
        when (finaleTick) {
            0 -> {
                synth.note(Patch.KICK, 36f, 0.9f, 0.1f, 0f)
                synth.note(Patch.BASS, 45f, 0.7f, beatSec * 2f, 0f)
                for (d in v.indices) { motifTick[d] = 0; motifVel[d] = 0.7f }
            }
            8 -> for (k in 0..2) synth.note(Patch.PAD, chordTone(4, k, 57), 0.8f, beatSec * 2f, 0f)
            16 -> { bigChord(v, beatSec); layersOn = false }
        }
        finaleTick = if (finaleTick > 64) -1 else finaleTick + 1
    }

    private fun playEncore(v: Array<Voice>) {
        val beatSec = 60f / bpm
        val step = encoreTick % 16
        val bar = encoreTick / 16
        val n = v.size
        when {
            bar <= n -> {
                val deg = PROGRESSION[bar % 4].let { if (bar == n) 4 else it }
                if (step == 0) chordDeg = deg
                if (step == 0 || step == 8) synth.note(Patch.BASS, chordTone(chordDeg, 0, 38), 0.55f, beatSec * 1.5f, 0f)
                if (step == 0) for (k in 0..2) synth.note(Patch.PAD, chordTone(chordDeg, k, 57), 0.5f, beatSec * 3.8f, 0f)
                if (bar > 0 && step % 4 == 0) synth.note(Patch.KICK, 36f, 0.5f, 0.1f, 0f)
                if (bar > 0 && step % 4 == 2) synth.note(Patch.HAT, 90f, 0.35f, 0.03f, 0.15f)
                for (d in 0 until min(bar, n)) {
                    if (step % 2 == 0 && d != bar - 1) {
                        synth.note(v[d].patch, chordTone(chordDeg, (step / 2 + d) % 4, v[d].base), 0.2f, 0.12f, v[d].pan)
                    }
                }
                if (bar in 1..n && step == 0) { motifTick[bar - 1] = 0; motifVel[bar - 1] = 0.7f }
            }
            bar == n + 1 && step == 0 -> bigChord(v, beatSec)
            bar > n + 2 -> { encoreTick = -1; return }
        }
        encoreTick++
    }
}
