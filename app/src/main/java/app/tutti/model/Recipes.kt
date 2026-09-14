package app.tutti.model

import androidx.compose.ui.graphics.Color

/** Each dish is voiced by one instrument, seated somewhere in the stereo field like an orchestra. */
enum class Instrument(val label: String, val pan: Float, val baseMidi: Int) {
    CELLO("Cello", 0.40f, 50),
    HARP("Harp", -0.55f, 62),
    MARIMBA("Marimba", -0.10f, 62),
    FLUTE("Flute", -0.35f, 74),
    CELESTA("Celesta", 0.25f, 74),
}

/**
 * What the cook's hands (or the heat) are doing. Every action carries a tempo marking:
 * the music's pace is the pace of the work.
 */
enum class Action(val marking: String, val bpm: Int, val hint: String) {
    PREP("Moderato", 92, "prep at an easy pace"),
    CHOP("Allegro", 104, "chop on the beat"),
    WHISK("Presto", 126, "whisk with the sixteenths"),
    STIR("Andante", 84, "move with the phrase"),
    SEAR("Vivace", 112, "keep it moving"),
    PLATE("Maestoso", 80, "finish with ceremony"),
    HEAT("Crescendo", 76, "let it come up to heat"),
    BOIL("Agitato", 76, "let it bubble"),
    SIMMER("Adagio", 72, "low and slow"),
    BAKE("Largo", 72, "the oven has it"),
    REST("Fermata", 68, "hold, let it rest"),
}

data class Step(
    val title: String,
    val detail: String,
    val seconds: Int,
    val handsOn: Boolean,
    val action: Action,
    /** Longest the cook may wait between the previous step ending and this one starting. */
    val maxGapBefore: Int = 0,
)

data class Recipe(
    val id: String,
    val name: String,
    val note: String,
    val instrument: Instrument,
    val color: Color,
    /** Leitmotif as major-pentatonic scale indices (0 = tonic, 5 = tonic an octave up). */
    val motif: List<Int>,
    /** How early the dish may finish and still be served at its best. */
    val holdSeconds: Int,
    val steps: List<Step>,
) {
    val totalSeconds: Int get() = steps.sumOf { it.seconds }
    val handsOnSeconds: Int get() = steps.filter { it.handsOn }.sumOf { it.seconds }
}

private const val ANYTIME = 60 * 60
private fun min(m: Double) = (m * 60).toInt()

object RecipeBook {
    val salmon = Recipe(
        id = "salmon",
        name = "Garlic-Butter Salmon",
        note = "Crisp skin, basted in brown butter",
        instrument = Instrument.CELLO,
        color = Color(0xFFE0775B),
        motif = listOf(0, 3, 4, 3),
        holdSeconds = 90,
        steps = listOf(
            Step("Pat dry & season", "Pat the fillets bone-dry, then salt and pepper both sides.", min(3.0), true, Action.PREP),
            Step("Mince garlic, chop parsley", "Three cloves, finely. A small handful of parsley.", min(3.0), true, Action.CHOP, ANYTIME),
            Step("Heat the skillet", "Oil in a heavy pan over medium-high until it shimmers.", min(2.0), false, Action.HEAT, ANYTIME),
            Step("Sear skin-side down", "Lay the fillets in, away from you. Don't touch them.", min(4.0), false, Action.SEAR, 30),
            Step("Flip & baste", "Flip, add butter and garlic, spoon it over and over.", min(2.0), true, Action.SEAR, 0),
            Step("Rest off the heat", "Onto a warm plate. Carryover heat finishes the center.", min(3.0), false, Action.REST, 0),
        ),
    )

    val rice = Recipe(
        id = "rice",
        name = "Lemon-Herb Rice",
        note = "Fluffy, bright, forgiving",
        instrument = Instrument.HARP,
        color = Color(0xFFE2B340),
        motif = listOf(5, 4, 2, 3),
        holdSeconds = 300,
        steps = listOf(
            Step("Rinse the rice", "One cup, cold water, swirl until it runs clear.", min(2.0), true, Action.PREP),
            Step("Boil the water", "Two cups water and a pinch of salt, lid on.", min(5.0), false, Action.BOIL, ANYTIME),
            Step("Rice in, heat low", "Stir once, lid on, lowest flame.", min(1.0), true, Action.STIR, 60),
            Step("Simmer, lid on", "No peeking. The steam does the work.", min(15.0), false, Action.SIMMER, 0),
            Step("Rest, still covered", "Off the heat. The lid stays on.", min(5.0), false, Action.REST, 0),
            Step("Fluff with lemon & herbs", "Zest of a lemon, torn herbs, a knob of butter.", min(1.0), true, Action.STIR, 300),
        ),
    )

    val broccoli = Recipe(
        id = "broccoli",
        name = "Charred Broccoli",
        note = "Blistered edges, chili, parmesan",
        instrument = Instrument.MARIMBA,
        color = Color(0xFF5F9A64),
        motif = listOf(3, 3, 4, 5),
        holdSeconds = 180,
        steps = listOf(
            Step("Preheat oven to 425°F", "Rack in the upper third.", min(10.0), false, Action.HEAT),
            Step("Cut into florets", "Bite-size with a flat side. Flat sides char best.", min(4.0), true, Action.CHOP, ANYTIME),
            Step("Toss with oil & chili", "Two tablespoons oil, salt, a pinch of chili flakes.", min(1.0), true, Action.PREP, ANYTIME),
            Step("Roast", "Single layer on a sheet pan, top rack.", min(10.0), false, Action.BAKE, ANYTIME),
            Step("Shake the pan", "Flip the florets so the other side chars.", min(0.5), true, Action.PREP, 0),
            Step("Roast until charred", "Edges should look almost burnt. That's the flavor.", min(8.0), false, Action.BAKE, 0),
            Step("Lemon & parmesan", "A squeeze of lemon, a snowfall of parmesan.", min(1.0), true, Action.PLATE, 120),
        ),
    )

    val caesar = Recipe(
        id = "caesar",
        name = "Little Gem Caesar",
        note = "Whisked dressing, torn croutons",
        instrument = Instrument.FLUTE,
        color = Color(0xFF93AE5E),
        motif = listOf(7, 6, 5, 3),
        holdSeconds = 120,
        steps = listOf(
            Step("Whisk the dressing", "Yolk, lemon, anchovy, garlic, then oil in a thin stream.", min(4.0), true, Action.WHISK),
            Step("Tear the lettuce & bread", "Hand-torn leaves, spun very dry. Bread in rough cubes.", min(3.0), true, Action.CHOP, ANYTIME),
            Step("Toast the croutons", "Olive oil, salt, into the hot oven.", min(6.0), false, Action.BAKE, ANYTIME),
            Step("Dress & toss", "Coat every leaf. Croutons on top.", min(1.5), true, Action.STIR, ANYTIME),
        ),
    )

    val mugCake = Recipe(
        id = "mugcake",
        name = "Molten Mug Cake",
        note = "Ninety seconds to chocolate",
        instrument = Instrument.CELESTA,
        color = Color(0xFF8A5A44),
        motif = listOf(5, 7, 6, 8),
        holdSeconds = 240,
        steps = listOf(
            Step("Whisk the batter", "Flour, cocoa, sugar, milk, oil, one square of chocolate.", min(3.0), true, Action.WHISK),
            Step("Microwave", "Ninety seconds on high. It should still wobble.", min(1.5), false, Action.BAKE, ANYTIME),
            Step("Let it set", "One minute. The center stays molten.", min(1.0), false, Action.REST, 0),
        ),
    )

    val all = listOf(salmon, rice, broccoli, caesar, mugCake)
}
