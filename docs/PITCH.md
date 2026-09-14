# Tutti: presentation kit

Slide-by-slide copy for the Adobe Express template, plus a demo video script.

- **Finished slides:** `docs/deck/slides/01.png` to `12.png` (1920×1080). Import them into Adobe Express as full-bleed images, or rebuild the copy on the template. To regenerate after editing `docs/deck/deck.html`, run `bash docs/deck/render.sh`.
- **Screenshots:** `docs/screenshots/` (full-resolution phone captures, plus `hero.png` with four screens side by side)
- **Demo video:** `docs/demo/tutti-demo.mp4` (2.5 minutes, screen plus the app's own audio, rehearsal mode at 20×)

---

## Slide 1 · Title
**Tutti**
*Cook in concert.*
Food × Music · Android · TXST Shipaton 2026

Visual: `hero.png` (four screens side by side), or the fermata app icon on ink.

## Slide 2 · The problem
**Dinner is an orchestra with no conductor.**
Three dishes means three timers, and you have one pair of hands. The salmon needs flipping *now*, the rice has to rest, the broccoli is charring. Timers beep, but they don't say which dish, what to do, or how long you have. People who struggle with time (new cooks, anyone with ADHD time blindness, anyone with flour on their hands) get cold sides and burnt mains.

## Slide 3 · The collision
**What becomes possible only because Food and Music collided?**
- **The recipe writes the music.** Steps become sections, dishes become instruments, and the pace of the work sets the tempo.
- **The music runs the kitchen.** A new instrument entering means a dish needs you, a rising leitmotif means the moment is coming, and the final chord means serve.

You don't read the kitchen. You *hear* it.

## Slide 4 · How it works
1. **Programme:** pick your dishes like movements in a concert.
2. **Score:** Tutti schedules everything backwards from the final chord, and never gives you two hands-on tasks at once.
3. **Conduct:** a live arrangement follows every dish. Chop on the beat, and listen for your cue.
4. **Tutti:** every dish lands on the same final chord, and the encore plays your dinner back as a song.

## Slide 5 · The score (`02-score.png`)
**38 minutes, one final chord.**
Solid notes are your hands. Hatched notes are the heat. The "you" lane proves there are never two hands-on tasks at once. Tap an instrument to learn its leitmotif before you cook.

## Slide 6 · The stage (`04-hands-on.png`, `06-listen.png`)
**The tempo is the pace of the work.**
- *Allegro* 104 bpm: chop on the beat.
- *Presto* 126 bpm: whisk with the sixteenths.
- *Adagio*: the rice simmers as a quiet drone you can hear.
- Before every cue, the dish's leitmotif builds, the harmony leans in, and then a bell rings and the phone buzzes.

## Slide 7 · Life happens (`05-rescored.png`)
**Running late? The score rewrites itself.**
Tap *+1 minute* or *Done*. Anything already on the heat stays put, and everything else is re-planned in milliseconds. Tutti tells you whether the final chord holds or moves.

## Slide 8 · Finale and encore (`07-tutti.png`, `08-finale.png`)
**Tutti.** *Every dish, together.*
The encore turns tonight's dinner into a short piece, with each leitmotif entering in the order you cooked it.

## Slide 9 · Impact
- **New cooks:** multi-dish meals without the panic.
- **Time blindness and ADHD:** time you can *feel* through tempo and build-ups, not numbers you have to track.
- **Low-vision and hands-busy cooking:** audio-first cues and haptics, with huge type when you do look.
- **Families and holidays:** the big-meal problem (everything hot at once) is exactly the problem Tutti solves.
- **Fewer burnt dinners and takeout nights:** confidence is what turns "too hard" into "let's cook."

## Slide 10 · Under the hood
- **Native Android:** Kotlin and Jetpack Compose, with every screen custom-drawn (no Material components).
- **Synthesizer built from scratch:** 48 voices and 12 instruments (cello, harp, marimba, flute, celesta, FM bells and more), plus reverb. Rendering allocates no memory, and the app ships **zero audio files**.
- **Sample-accurate sequencer** on a real-time audio thread, with tempo changes on bar lines.
- **Backward scheduler** over a single hands-on resource, with lookahead, hold tolerances and live re-planning.

## Slide 11 · What's next
- Say "done" out loud instead of tapping.
- Import any recipe URL and have it scored automatically.
- Smart ovens and thermometers that report their own cues.
- Ensemble mode: two cooks, two hands lanes, one score.
- A haptic conductor on your watch.

## Slide 12 · Close
**When you hear *tutti*, you serve.**
GitHub: `<your repo link>`

---

## Demo video script (about 75 seconds)

| Time | Shot | Voice-over / caption |
|---|---|---|
| 0:00 | Programme screen, scroll the movements | "Three dishes, three timers, one pair of hands. Tutti turns dinner into music." |
| 0:08 | Tap Caesar on and off, then tap Compose | "Pick your dishes. Each one gets its own instrument." |
| 0:14 | Score screen, then tap Cello, Harp, Marimba | "Tutti schedules backwards from the final chord, so you're never doing two things at once. Every dish has a leitmotif." |
| 0:24 | Rehearsal on, then Raise the baton, count-in 1-2-3-4 | "Raise the baton." |
| 0:30 | Stage: "Listen for the harp," with the playhead moving | "While the heat works, you hear it working. When a dish needs you, its theme builds…" |
| 0:40 | Hands-on panel: *Allegro*, beat dots | "…the bell rings, and you chop on the beat." |
| 0:50 | Tap +1 minute, and the notice appears | "Running late? The score rewrites itself live." |
| 1:00 | The final chord, "Tutti." overlay | "Every dish lands on the same final chord." |
| 1:08 | Finale screen, tap Play the encore | "And your dinner plays back as a song. Tutti. Cook in concert." |

Record with sound: the included `docs/demo/` video pairs the screen capture with the app's own recorded audio.
