package app.tutti.schedule

import app.tutti.model.Recipe
import app.tutti.model.RecipeBook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConductorTest {

    /** Every non-empty menu the programme can produce (31 combinations of 5 dishes). */
    private val menus: List<List<Recipe>> = (1 until (1 shl RecipeBook.all.size)).map { mask ->
        RecipeBook.all.filterIndexed { i, _ -> mask and (1 shl i) != 0 }
    }

    private fun assertOneLane(score: Score, context: String) {
        val hands = score.slots.filter { score.stepOf(it).handsOn }.sortedBy { it.start }
        hands.zipWithNext().forEach { (a, b) ->
            assertTrue("two hands-on tasks overlap in $context: $a / $b", b.start >= a.end)
        }
    }

    @Test
    fun theCookNeverHoldsTwoTasksAtOnce() {
        for (menu in menus) assertOneLane(Conductor.compose(menu), menu.map { it.id }.toString())
    }

    @Test
    fun everyDishLandsOnTheFinalChord() {
        for (menu in menus) {
            val score = Conductor.compose(menu)
            menu.forEachIndexed { d, recipe ->
                val hold = score.holdFor(d)
                assertTrue("${recipe.id} waits ${hold}s in ${menu.map { it.id }}", hold in 0..recipe.holdSeconds)
            }
        }
    }

    @Test
    fun stepsKeepTheirOrderAndTheirGaps() {
        for (menu in menus) {
            val score = Conductor.compose(menu)
            assertEquals("strain in ${menu.map { it.id }}", 0, score.strain)
            menu.forEachIndexed { d, recipe ->
                val slots = score.slotsFor(d)
                assertEquals(recipe.steps.size, slots.size)
                slots.zipWithNext().forEach { (a, b) ->
                    assertTrue(b.start >= a.end)
                    assertTrue(
                        "${recipe.id} step ${b.step} waits too long in ${menu.map { it.id }}",
                        b.start - a.end <= recipe.steps[b.step].maxGapBefore,
                    )
                }
            }
        }
    }

    @Test
    fun anExtraMinuteIsRescoredWithoutBreakingTheRules() {
        val score = Conductor.compose(listOf(RecipeBook.salmon, RecipeBook.rice, RecipeBook.broccoli))
        for (cue in score.cues.filter { score.stepOf(it).handsOn }) {
            val now = cue.start + 10
            val fixed = score.slots.filter { it.start <= now }.map { if (it == cue) it.copy(end = it.end + 60) else it }
            val rescored = Conductor.reflow(score, now, fixed)
            assertTrue(rescored.tutti >= score.tutti)
            assertOneLane(rescored, "reflow after ${score.stepOf(cue).title}")
            rescored.slots.filter { it !in fixed }.forEach { assertTrue("$it starts in the past", it.start >= now) }
            assertEquals(score.slots.size, rescored.slots.size)
        }
    }

    @Test
    fun finishingEarlyKeepsTheFinalChord() {
        val score = Conductor.compose(listOf(RecipeBook.salmon, RecipeBook.rice, RecipeBook.broccoli))
        val cue = score.cues.first { score.stepOf(it).handsOn && it.length >= 120 }
        val now = cue.start + 30
        val fixed = score.slots.filter { it.start <= now }.map { if (it == cue) it.copy(end = now) else it }
        val rescored = Conductor.reflow(score, now, fixed)
        assertEquals(score.tutti, rescored.tutti)
        assertOneLane(rescored, "finish early")
    }
}
