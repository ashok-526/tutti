# Tutti: presentation kit

Slide copy for the Adobe Express template, plus a demo video script.

- **Finished slides:** `docs/deck/slides/01.png` to `12.png` (1920×1080). Import them into Adobe Express as full-bleed images, or rebuild the copy on the template. To regenerate after editing `docs/deck/deck.html`, run `bash docs/deck/render.sh`.
- **Screenshots:** `docs/screenshots/` (full-resolution phone captures, plus `hero.png` with four screens side by side)
- **Demo video:** `docs/demo/tutti-demo.mp4` (screen plus the app's own audio, rehearsal mode at 20×)

---

## 1 · Title
**Tutti.** Dinner, pressed as one record.
Every dish becomes a groove with its own instrument. The tonearm tracks the cues, and when the needle reaches the label, everything is ready.
Visual: `hero.png`, or `01-programme.png` beside `04-hands-on.png`.

## 2 · The problem
**Three dishes. Three timers. One pair of hands.**
Timers beep, but they can't tell you which dish, what to do, or how long you have. It's hardest on new cooks, on anyone with ADHD time blindness, and on anyone with flour on their hands.

## 3 · The collision (Food × Music)
**The recipe presses the record. The record runs the kitchen.**
- Food writes the music: each dish is a slice of the record with its own instrument; hands-on steps are solid bands and heat is fine grooves; the action sets the tempo (chopping is Allegro, simmering is Adagio).
- Music conducts the cook: a theme builds when a dish is about to need you, the amber light means your hands now, and when the needle meets the label the final chord plays. Serve.

## 4 · How it works
Pick from the crate (`01-programme.png`), press the record (`02-score.png`), drop the needle (`04-hands-on.png`), serve (`08-finale.png`).

## 5 · The record (`02-score.png`)
**Time runs inward, like a record.** Each dish owns a slice of the disc. Solid bands are your hands, fine grooves are the heat, and the label is the final chord. The scheduler never gives you two hands-on tasks at once; unit tests check all 31 possible menus.

## 6 · Playing (`04-hands-on.png`, `06-listen.png`)
**The tempo is the pace of the work.** Allegro 104 bpm: chop on the beat. Presto 126: whisk in sixteenths. Andante 84: stir with the phrase. Adagio 72: the rice simmers as a quiet drone. The tonearm tracks inward, the strobe dots pulse on every beat, and the amber light means your hands, now.

## 7 · Live re-scoring (`05-rescored.png`)
**Running late? The record re-presses.** Tap +1 minute or Done. Anything already on the heat stays put; everything not yet started is re-planned within each dish's tolerances, and Tutti says whether the final chord holds or moves.

## 8 · Finale (`07-tutti.png`, `08-finale.png`)
**The needle reaches the label.** The whole orchestra plays the final chord and the record spins down. Flip it over and the encore plays tonight's dinner back as a short song.

## 9 · Who it's for
New cooks, people with time blindness, anyone whose hands are busy, and anyone making a big meal where everything has to be hot at once.

## 10 · Under the hood
Kotlin and Jetpack Compose with Material 3; the record, tonearm, and label lettering are drawn on Canvas. A from-scratch synthesizer (48 voices, 12 instruments) and a sample-accurate sequencer. The app ships zero audio files. A backward scheduler with live re-scoring, tested across 31 menus.

## 11 · Side B (what's next)
Say "done" out loud. Press any recipe from a link. Smart ovens that report their own cues. Duets: two cooks, two lanes, one record. A cue light on your wrist.

## 12 · Close
**When the needle reaches the label, you serve.** github.com/ashok-526/tutti

---

## Demo video script (about 2.5 minutes)

| Time | Shot | Voice-over / caption |
|---|---|---|
| 0:00 | Tonight's record: scroll the crate | "Three dishes, three timers, one pair of hands. Tutti presses dinner onto one record." |
| 0:08 | Tap Press the record | "Each dish becomes a groove with its own instrument." |
| 0:12 | The record screen: tap Cello, Harp, Marimba | "Every dish has a four-note theme. That's how you'll know it needs you." |
| 0:20 | Rehearsal on, Drop the needle, count-in | "Drop the needle." |
| 0:25 | Hands free: next cue and the tonearm moving | "While the heat works, you hear it working." |
| 0:35 | Your hands: amber light, beat pips | "When a dish needs you, its theme builds and the amber light comes on. Chop on the beat." |
| 0:45 | Tap Done, then +1 minute: the snackbar | "Finished early or running late? The record re-presses itself." |
| 2:15 | The needle reaches the label | "When the needle reaches the label, everything is ready." |
| 2:20 | Finale, Play the encore | "Flip it over, and dinner plays back as a song. Tutti." |
