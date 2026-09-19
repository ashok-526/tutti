package app.tutti.session

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import app.tutti.audio.Orchestra
import app.tutti.model.Action
import app.tutti.model.Recipe
import app.tutti.model.RecipeBook
import app.tutti.schedule.Conductor
import app.tutti.schedule.Score
import app.tutti.schedule.Slot
import kotlinx.coroutines.delay
import java.io.File

enum class Screen { PROGRAMME, SCORE, CONDUCT, FINALE }

class TuttiState(private val context: Context) {
    val orchestra = Orchestra()
    val tipJar = TipJar(onThanks = { orchestra.tutti() })

    var screen by mutableStateOf(Screen.PROGRAMME)
        private set
    val selected = mutableStateListOf("salmon", "rice", "broccoli")
    var score by mutableStateOf<Score?>(null)
        private set
    var rehearsal by mutableStateOf(false)
    /** How much faster than real time a rehearsal runs. Demo captures raise it to fit a short video. */
    var rehearsalSpeed = 20f
    /** When the whole session is being recorded (for demo videos), performances don't manage the recording. */
    var sessionRecording = false
    var performance by mutableStateOf<Performance?>(null)
        private set

    val menu: List<Recipe> get() = selected.map { id -> RecipeBook.all.first { it.id == id } }

    fun toggle(recipe: Recipe) {
        if (recipe.id in selected) selected.remove(recipe.id) else selected.add(recipe.id)
    }

    fun compose() {
        if (selected.isEmpty()) return
        val composed = Conductor.compose(menu)
        score = composed
        orchestra.configure(composed.recipes)
        screen = Screen.SCORE
    }

    /** Let the cook meet a dish's leitmotif before the performance. */
    fun audition(dish: Int) = orchestra.cue(dish)

    fun raiseBaton() {
        val composed = score ?: return
        orchestra.configure(composed.recipes)
        performance = Performance(
            initial = composed,
            speed = if (rehearsal) rehearsalSpeed else 1f,
            orchestra = orchestra,
            buzz = ::buzz,
            recording = if (sessionRecording) null else File(context.getExternalFilesDir(null) ?: context.cacheDir, "performance.wav"),
        )
        screen = Screen.CONDUCT
    }

    fun finale() {
        screen = Screen.FINALE
        if (!sessionRecording) Handler(Looper.getMainLooper()).postDelayed({ orchestra.endRecording() }, 6_000)
    }

    /** Replay the dinner as a short piece: leitmotifs enter in the order the dishes began. */
    fun playEncore() {
        val composed = performance?.score ?: score ?: return
        val order = composed.recipes.indices.sortedBy { d -> composed.slotsFor(d).minOfOrNull { it.start } ?: 0 }
        orchestra.configure(order.map { composed.recipes[it] })
        orchestra.encore()
    }

    fun back() {
        when (screen) {
            Screen.SCORE -> screen = Screen.PROGRAMME
            Screen.CONDUCT -> {
                performance?.abandon()
                performance = null
                screen = Screen.SCORE
            }
            Screen.FINALE -> newProgramme()
            Screen.PROGRAMME -> Unit
        }
    }

    fun newProgramme() {
        performance?.abandon()
        performance = null
        score = null
        screen = Screen.PROGRAMME
    }

    private fun buzz(pattern: LongArray) {
        val vibrator = if (Build.VERSION.SDK_INT >= 31) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }
        runCatching { vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1)) }
    }
}

enum class Phase { WAITING, ACTIVE, COOKING, READY }

data class DishNow(val phase: Phase, val slot: Slot?, val secondsLeft: Int, val progress: Float)

data class Flash(val dish: Int, val step: Int, val at: Long)

/** One live performance of a score: the session clock, cues, and live re-scoring. */
class Performance(
    initial: Score,
    val speed: Float,
    private val orchestra: Orchestra,
    private val buzz: (LongArray) -> Unit,
    private val recording: File?,
) {
    var score by mutableStateOf(initial)
        private set
    val originalTutti = initial.tutti
    var now by mutableFloatStateOf(0f)
        private set
    var countIn by mutableIntStateOf(0)
        private set
    var started by mutableStateOf(false)
        private set
    var finished by mutableStateOf(false)
        private set
    var flash by mutableStateOf<Flash?>(null)
        private set
    var notice by mutableStateOf<String?>(null)
        private set
    var rescored by mutableIntStateOf(0)
        private set
    var cuesPlayed by mutableIntStateOf(0)
        private set

    private val fired = HashSet<Long>()
    private var launched = false
    private var abandoned = false

    val recipes: List<Recipe> get() = score.recipes

    suspend fun run() {
        if (launched) return
        launched = true
        orchestra.layersOn = false
        orchestra.targetBpm = COUNT_IN_BPM
        recording?.let {
            orchestra.record(it)
            Log.i("Tutti", "recording-start ${System.currentTimeMillis()}")
        }
        Log.i("Tutti", "countin-start ${System.currentTimeMillis()}")
        orchestra.countIn()
        val beat = (60_000f / COUNT_IN_BPM).toLong()
        for (b in 1..4) {
            countIn = b
            delay(beat)
        }
        countIn = 0
        started = true
        orchestra.layersOn = true
        var last = 0L
        while (!finished && !abandoned) {
            withFrameNanos { t ->
                if (last != 0L) now += (t - last) / 1e9f * speed
                last = t
            }
            update()
        }
    }

    fun abandon() {
        abandoned = true
        orchestra.layersOn = false
        if (recording != null) orchestra.endRecording()
    }

    private fun key(slot: Slot) = slot.dish * 1000L + slot.step

    private fun update() {
        val s = score
        val t = now
        for (slot in s.slots) {
            if (slot.start <= t && key(slot) !in fired) {
                fired += key(slot)
                orchestra.cue(slot.dish)
                cuesPlayed++
                flash = Flash(slot.dish, slot.step, System.nanoTime())
                buzz(if (s.stepOf(slot).handsOn) longArrayOf(0, 60, 80, 60) else longArrayOf(0, 40))
            }
        }

        val lead = minOf(12f * speed, 90f)
        var bpm = IDLE_BPM
        for (d in s.recipes.indices) {
            val slots = s.slotsFor(d)
            val current = slots.firstOrNull { it.start <= t && t < it.end }
            val next = slots.firstOrNull { it.start > t }
            val step = current?.let { s.stepOf(it) }
            when {
                slots.all { it.end <= t } -> orchestra.setDish(d, Orchestra.READY, Action.REST)
                step != null && step.handsOn -> {
                    orchestra.setDish(d, Orchestra.ACTIVE, step.action)
                    bpm = step.action.bpm.toFloat()
                }
                next != null && next.start - t < lead -> orchestra.setDish(
                    d, Orchestra.BUILD, (step ?: s.stepOf(next)).action, 1f - (next.start - t) / lead,
                )
                step != null -> orchestra.setDish(d, Orchestra.COOKING, step.action)
                else -> orchestra.setDish(d, Orchestra.SILENT, Action.REST)
            }
        }
        orchestra.targetBpm = bpm

        if (t >= s.tutti) {
            finished = true
            orchestra.tutti()
            buzz(longArrayOf(0, 120, 90, 120, 90, 320))
        }
    }

    fun currentHandsOn(): Slot? =
        score.slots.firstOrNull { score.stepOf(it).handsOn && it.start <= now && now < it.end }

    fun upcoming(limit: Int): List<Slot> = score.cues.filter { it.start > now }.take(limit)

    fun dishNow(dish: Int): DishNow = dishAt(dish, now)

    /** A dish's state at session time [t], without subscribing the caller to every clock tick. */
    fun dishAt(dish: Int, t: Float): DishNow {
        val slots = score.slotsFor(dish)
        val current = slots.firstOrNull { it.start <= t && t < it.end }
        val next = slots.firstOrNull { it.start > t }
        return when {
            slots.all { it.end <= t } -> DishNow(Phase.READY, slots.lastOrNull(), 0, 1f)
            current != null -> DishNow(
                if (score.stepOf(current).handsOn) Phase.ACTIVE else Phase.COOKING,
                current,
                (current.end - t).toInt(),
                ((t - current.start) / current.length.coerceAtLeast(1)).coerceIn(0f, 1f),
            )
            else -> DishNow(Phase.WAITING, next, ((next?.start ?: 0) - t).toInt(), 0f)
        }
    }

    /** The cook finished early: the score catches up to them. */
    fun done() {
        val current = currentHandsOn() ?: return
        val t = now.toInt()
        rescore(t) { if (it == current) it.copy(end = t) else it }
    }

    /** The cook needs another minute: everything not yet begun is re-written around it. */
    fun needMore() {
        val current = currentHandsOn() ?: return
        val t = now.toInt()
        rescore(t) { if (it == current) it.copy(end = it.end + 60) else it }
    }

    private fun rescore(t: Int, edit: (Slot) -> Slot) {
        val before = score.tutti
        val fixed = score.slots.filter { it.start <= t }.map(edit)
        score = Conductor.reflow(score, t, fixed)
        rescored++
        val shift = score.tutti - before
        notice = when {
            shift == 0 -> "Re-scored. The final chord holds."
            shift > 0 -> "Re-scored. The final chord moves ${shift / 60}:${"%02d".format(shift % 60)} later."
            else -> "Re-scored. The final chord comes early."
        }
    }

    companion object {
        const val COUNT_IN_BPM = 88f
        const val IDLE_BPM = 76f
    }
}
