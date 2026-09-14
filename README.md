# Tutti

**Cook in concert.** Tutti turns dinner into a piece of music. Every dish gets its own instrument and leitmotif, and the music tells you when to act, so everything reaches the table on the same final chord.

> *tutti* (Italian, "all together"): the moment in a score when the whole orchestra plays at once.

**TXST Shipaton 2026 · Food × Music** · Native Android · Kotlin + Jetpack Compose · Real-time synthesis engine written from scratch

<p align="center">
  <img src="docs/screenshots/01-programme.png" width="19%" alt="Programme: pick your dishes" />
  <img src="docs/screenshots/02-score.png" width="19%" alt="Score: the schedule as staves" />
  <img src="docs/screenshots/04-hands-on.png" width="19%" alt="Conduct: a hands-on cue" />
  <img src="docs/screenshots/05-rescored.png" width="19%" alt="Live re-scoring" />
  <img src="docs/screenshots/08-finale.png" width="19%" alt="Finale" />
</p>

🎬 **Demo video (with the app's own audio):** [`docs/demo/tutti-demo.mp4`](docs/demo/tutti-demo.mp4)

---

## The problem

Cooking one dish is following a recipe. Cooking three at once is conducting an orchestra. The salmon needs flipping *now*, the rice has to rest, the broccoli is charring, and every one of them is on its own timer. People who struggle with time (novice cooks, anyone with ADHD time blindness, anyone with dough on their hands) end up with cold sides and burnt mains. Kitchen timers beep, but they don't tell you which dish, what to do, or how long you have left.

## The collision

Tutti works because food and music depend on each other in both directions.

| Food → Music | Music → Food |
|---|---|
| The recipes *compile* into a score: every step becomes a musical section, every dish a stave. | The music *conducts* the cook: a new instrument entering means a dish needs you. |
| The action sets the tempo: chopping is *Allegro* (104 bpm), whisking *Presto* (126), simmering *Adagio*. | You chop, stir and baste on the beat. The pace of the music is the pace of the work. |
| Heat you don't have to watch (simmer, roast) becomes a quiet drone, so you can hear that it's cooking. | A dish's leitmotif builds for a few bars before it needs you. You feel the moment coming without looking at a screen. |
| Tell Tutti you're running late (+1 minute) and the scheduler rewrites the rest of the score live. | The final chord lands when everything is ready. When you hear *tutti*, you serve. |

Timers beep; Tutti *sings*. Hearing that the harp has come in is faster, calmer and more hands-free than reading three countdowns.

## How it works

1. **Programme.** Pick your dishes like movements in a concert programme. Each dish is scored for its own instrument: salmon for cello, rice for harp, broccoli for marimba, salad for flute, mug cake for celesta.
2. **Score.** Tutti's scheduler works *backwards* from the final chord. It keeps a single lane for your hands (you never get two hands-on tasks at once, with a 30-second breath between them) and places passive heat as late as possible. It also respects each recipe's tolerances: how long ingredients can wait between steps, and how early a dish can finish and still be served at its best. The score screen shows the plan as staves, with solid notes for your hands, hatched notes for the heat, and a double bar where everything lands. Tap an instrument to hear its leitmotif before you start.
3. **Conduct.** Raise the baton for a four-beat count-in, and the kitchen becomes a stage. A live, generative arrangement in D major follows exactly what every dish is doing:
   - **Hands-on steps** play a rhythmic ostinato on the dish's instrument, shaped by the action (staccato for chopping, flowing arpeggios for stirring, syncopation and sizzle for searing) over a kick you can work to.
   - **Passive steps** play quiet textures: bubbling for boiling, long sustained notes for roasting, a soft recurring note for resting.
   - **Before a cue** the dish's leitmotif enters and grows louder as the moment gets closer, and the harmony tightens onto the dominant.
   - **On the cue** a bell rings, the leitmotif plays out, and the phone buzzes. The screen shows the instruction in large type, with the tempo marking and beat dots.
4. **Re-score live.** Finished early? Tap *Done*. Need more time? Tap *+1 minute*. Anything already on the heat stays fixed. Everything not yet begun is re-planned from that moment, and Tutti tells you whether the final chord holds or moves.
5. **Finale and encore.** When the last dish is ready, the whole orchestra plays the final chord together. The encore plays your dinner back as a short piece, with each leitmotif entering in the order you cooked it.

**Rehearsal mode** runs a performance at 20× speed, so you can see and hear the whole dinner in about two minutes.

## Engineering

- **Scheduler** (`schedule/Conductor.kt`): a backward, latest-start placement over a single hands-on resource. It uses one step of lookahead so it doesn't take a slot another dish can't live without, and a repair loop that uses each dish's hold tolerance when a tight chain collides with another dish. The same solver re-plans mid-performance, taking steps already started as fixed and "no earlier than now" as a hard floor.
- **Synthesizer** (`audio/Synth.kt`): a 48-voice polyphonic synth written from scratch, rendering float PCM sample by sample. It has twelve patches: filtered-saw cello, plucked harp, marimba with an inharmonic partial, breathy flute, celesta, FM bell, a detuned pad, bass, kick, hat and shaker. Voices are stored as preallocated primitive arrays, so rendering allocates nothing. A trimmed Freeverb adds the room.
- **Orchestra** (`audio/Orchestra.kt`): a sample-accurate 16th-note sequencer on a dedicated `URGENT_AUDIO` thread feeding `AudioTrack`. Tempo changes land on bar lines. The UI thread only publishes what each dish is doing; every musical decision is made on the audio thread. It can also record the performance to WAV.
- **UI** (`ui/`): Jetpack Compose with no Material components. The type is Instrument Serif and IBM Plex Mono, the palette is a concert programme (paper and ink) that turns into a dark stage while you cook, the score and notation are drawn on Canvas, and beat-synced motion reads the audio clock every frame.

```
app/src/main/java/app/tutti/
├── model/Recipes.kt        dishes, steps, instruments, tempo markings
├── schedule/Conductor.kt   backward scheduler + live re-scoring
├── audio/Synth.kt          polyphonic synthesizer + reverb
├── audio/Orchestra.kt      sequencer, arrangement rules, AudioTrack thread
├── session/Performance.kt  app state, session clock, cues, haptics
└── ui/                     Programme, Score, Conduct, Finale screens
```

## Build and run

Requirements: JDK 17 and the Android SDK (compileSdk 35).

```bash
./gradlew :app:installDebug
```

Or open the folder in Android Studio and run the `app` configuration. Tutti works on Android 8.0+ (API 26). Put your phone somewhere you can hear it, or use a Bluetooth speaker in the kitchen.

## Credits

Built in one day at TXST Shipaton. Fonts: [Instrument Serif](https://fonts.google.com/specimen/Instrument+Serif) and [IBM Plex Mono](https://fonts.google.com/specimen/IBM+Plex+Mono), both SIL Open Font License. All music is synthesized live on the device; the app ships no audio files.
