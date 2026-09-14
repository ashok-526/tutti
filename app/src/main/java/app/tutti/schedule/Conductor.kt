package app.tutti.schedule

import app.tutti.model.Recipe

/** One step placed on the timeline. Times are seconds on the session clock (0 = downbeat). */
data class Slot(val dish: Int, val step: Int, val start: Int, val end: Int) {
    val length: Int get() = end - start
}

data class Score(
    val recipes: List<Recipe>,
    val slots: List<Slot>,
    /** The final chord: when every dish is served. */
    val tutti: Int,
    /** Seconds of waiting the plan could not avoid (food sitting longer than its recipe allows). */
    val strain: Int = 0,
) {
    fun stepOf(slot: Slot) = recipes[slot.dish].steps[slot.step]
    fun slotsFor(dish: Int) = slots.filter { it.dish == dish }.sortedBy { it.step }
    val downbeat: Int get() = slots.minOfOrNull { it.start } ?: 0
    val handsOnSeconds: Int get() = slots.filter { stepOf(it).handsOn }.sumOf { it.length }
    val cues: List<Slot> get() = slots.sortedWith(compareBy({ it.start }, { it.dish }))
    fun holdFor(dish: Int): Int = tutti - (slotsFor(dish).maxOfOrNull { it.end } ?: tutti)
}

/**
 * Where a dish stands when the score is (re)written mid-performance.
 * [nextStep] is the first step not yet started; [prevEnd] is when the step before it ends or ended.
 */
data class DishPlan(
    val recipe: Recipe,
    val nextStep: Int = 0,
    val prevEnd: Int? = null,
    val earliest: Int = Int.MIN_VALUE,
)

/**
 * The scheduler. Works backwards from the final chord so every dish lands on it, while
 * guaranteeing the cook never has two hands-on tasks at once.
 */
object Conductor {
    const val GRID = 30
    /** Preferred breathing room between two different dishes' hands-on tasks. */
    const val BREATH = 30

    private class Lane(val dish: Int, val start: Int, val end: Int)

    private class Attempt(val slots: List<Slot>, val strain: Int, val tooEarly: Boolean)

    /** Compose a fresh score for a menu. The downbeat is at t = 0. */
    fun compose(recipes: List<Recipe>): Score {
        val plans = recipes.map { DishPlan(it) }
        val (_, best) = solve(plans, target = 0, busy = emptyList(), mayDelay = false)
        val shift = -(best.slots.minOfOrNull { it.start } ?: 0)
        return Score(
            recipes = recipes,
            slots = best.slots.map { it.copy(start = it.start + shift, end = it.end + shift) }
                .sortedBy { it.start },
            tutti = shift,
            strain = best.strain,
        )
    }

    /**
     * Rewrite the rest of the score live. [fixed] holds every slot that has already begun
     * (possibly with an edited end). Tutti holds its time if it can; otherwise it moves later.
     */
    fun reflow(score: Score, now: Int, fixed: List<Slot>): Score {
        val plans = score.recipes.mapIndexed { d, recipe ->
            val begun = fixed.filter { it.dish == d }
            val next = (begun.maxOfOrNull { it.step } ?: -1) + 1
            val prevEnd = begun.maxByOrNull { it.step }?.end
            DishPlan(recipe, next, prevEnd, maxOf(now, prevEnd ?: now))
        }
        val busy = fixed.filter { score.stepOf(it).handsOn && it.end > now }
            .map { Lane(it.dish, it.start, it.end) }
        val floor = fixed.maxOfOrNull { it.end } ?: now
        val (tutti, best) = solve(plans, maxOf(score.tutti, floor), busy, mayDelay = true)
        return score.copy(
            slots = (fixed + best.slots).sortedBy { it.start },
            tutti = tutti,
            strain = best.strain,
        )
    }

    private fun solve(plans: List<DishPlan>, target: Int, busy: List<Lane>, mayDelay: Boolean): Pair<Int, Attempt> {
        var tutti = target
        var fallback: Pair<Int, Attempt>? = null
        val maxDelaySteps = if (mayDelay) 120 else 0
        for (delay in 0..maxDelaySteps) {
            val best = bestAt(plans, tutti, busy)
            if (best != null) {
                if (best.strain == 0) return tutti to best
                if (fallback == null || best.strain < fallback.second.strain) fallback = tutti to best
                // Serving a few minutes later is worth it if it means nothing sits waiting.
                if (!mayDelay || delay >= 10) break
            }
            tutti += GRID
        }
        return fallback ?: (tutti to attempt(plans, tutti, IntArray(plans.size), busy, BREATH))
    }

    /** Plan with breathing room between tasks; give it up only when that's what keeps food from waiting. */
    private fun bestAt(plans: List<DishPlan>, tutti: Int, busy: List<Lane>): Attempt? {
        val relaxed = descend(plans, tutti, busy, BREATH)
        if (relaxed != null && relaxed.strain == 0) return relaxed
        val tight = descend(plans, tutti, busy, 0) ?: return relaxed
        return if (relaxed == null || tight.strain < relaxed.strain) tight else relaxed
    }

    /**
     * Hill-climb over how early each dish finishes (within its hold tolerance). Each round tries
     * letting every dish finish one grid step earlier and keeps the move that removes the most strain.
     * Sideways moves are allowed, so the climb can cross plateaus; offsets only grow, so it terminates.
     */
    private fun descend(plans: List<DishPlan>, tutti: Int, busy: List<Lane>, pad: Int): Attempt? {
        val offsets = IntArray(plans.size)
        var best = attempt(plans, tutti, offsets, busy, pad)
        if (best.tooEarly) return null
        repeat(48) {
            if (best.strain == 0) return best
            var move = -1
            var next: Attempt? = null
            for (d in plans.indices) {
                if (offsets[d] + GRID > plans[d].recipe.holdSeconds) continue
                offsets[d] += GRID
                val a = attempt(plans, tutti, offsets, busy, pad)
                offsets[d] -= GRID
                if (!a.tooEarly && (next == null || a.strain < next.strain)) {
                    next = a
                    move = d
                }
            }
            val chosen = next ?: return best
            if (chosen.strain > best.strain) return best
            offsets[move] += GRID
            best = chosen
        }
        return best
    }

    private fun attempt(plans: List<DishPlan>, tutti: Int, offsets: IntArray, busy0: List<Lane>, pad: Int): Attempt {
        val n = plans.size
        val lanes = ArrayList(busy0)
        val slots = ArrayList<Slot>()
        val idx = IntArray(n) { plans[it].recipe.steps.lastIndex }
        val hi = IntArray(n) { tutti - offsets[it] }
        val lo = IntArray(n) { tutti - plans[it].recipe.holdSeconds }
        val done = BooleanArray(n) { idx[it] < plans[it].nextStep }
        var strain = 0
        var tooEarly = false

        fun place(d: Int, start: Int, end: Int) {
            val plan = plans[d]
            val step = plan.recipe.steps[idx[d]]
            slots += Slot(d, idx[d], start, end)
            if (step.handsOn) lanes += Lane(d, start, end)
            if (idx[d] == plan.nextStep) {
                if (start < plan.earliest) tooEarly = true
                val prev = plan.prevEnd
                if (prev != null && start - prev > step.maxGapBefore) strain += start - prev - step.maxGapBefore
                done[d] = true
            } else {
                hi[d] = start
                lo[d] = start - step.maxGapBefore
                idx[d]--
            }
        }

        fun floorFor(d: Int): Int {
            val step = plans[d].recipe.steps[idx[d]]
            val bound = if (idx[d] == plans[d].nextStep) plans[d].earliest else Int.MIN_VALUE
            return maxOf(lo[d], if (bound == Int.MIN_VALUE) Int.MIN_VALUE else bound + step.seconds)
        }

        while (true) {
            // Heat never argues with anyone: place passive steps as late as they can go.
            var moved = true
            while (moved) {
                moved = false
                for (d in 0 until n) {
                    if (done[d]) continue
                    val step = plans[d].recipe.steps[idx[d]]
                    if (!step.handsOn) {
                        place(d, hi[d] - step.seconds, hi[d])
                        moved = true
                    }
                }
            }
            val open = (0 until n).filter { !done[it] }
            if (open.isEmpty()) break

            fun fit(d: Int, extra: Lane? = null): Int? =
                latestFit(d, plans[d].recipe.steps[idx[d]].seconds, hi[d], floorFor(d), lanes, extra, pad)

            val fits = open.associateWith { fit(it) }
            // Tightest window first (earliest-deadline-first, run backwards), then whoever can sit latest.
            val order = open.sortedWith(
                compareByDescending<Int> { if (fits[it] == null) Int.MIN_VALUE else floorFor(it) }
                    .thenByDescending { fits[it] ?: Int.MIN_VALUE },
            )
            // One step of lookahead: don't take a slot another dish can't live without.
            val choice = order.firstOrNull { c ->
                val end = fits[c] ?: return@firstOrNull false
                val lane = Lane(c, end - plans[c].recipe.steps[idx[c]].seconds, end)
                open.all { o -> o == c || fits[o] == null || fit(o, lane) != null }
            } ?: order.first()

            val step = plans[choice].recipe.steps[idx[choice]]
            val end = fits[choice] ?: run {
                val forced = latestFit(choice, step.seconds, hi[choice], Int.MIN_VALUE, lanes, null, pad) ?: hi[choice]
                strain += (floorFor(choice) - forced).coerceAtLeast(GRID)
                forced
            }
            place(choice, end - step.seconds, end)
        }
        return Attempt(slots, strain, tooEarly)
    }

    private fun overlaps(l: Lane, dish: Int, start: Int, end: Int, pad: Int): Boolean {
        val gap = if (l.dish == dish) 0 else pad
        return start < l.end + gap && l.start < end + gap
    }

    /** Latest end time in [floor, hi] where a hands-on task of [length] fits the cook's single lane. */
    private fun latestFit(dish: Int, length: Int, hi: Int, floor: Int, lanes: List<Lane>, extra: Lane?, pad: Int): Int? {
        var end = hi
        while (true) {
            if (floor != Int.MIN_VALUE && end < floor) return null
            val start = end - length
            val clash = lanes.filter { overlaps(it, dish, start, end, pad) } +
                listOfNotNull(extra?.takeIf { overlaps(it, dish, start, end, pad) })
            if (clash.isEmpty()) return end
            val first = clash.minBy { it.start }
            end = first.start - if (first.dish == dish) 0 else pad
            if (end < -24 * 3600) return null
        }
    }
}
