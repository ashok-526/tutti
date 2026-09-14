# Product

<!-- impeccable:product-schema 1 -->

> Written without an interview: the user asked not to be asked questions. Every fact below is inferred from the original hackathon brief and the working build, and should be confirmed.

## Platform

android

## Users

Home cooks making a multi-dish meal (usually two to five dishes) who struggle to get everything ready at once: new cooks, people with ADHD time blindness, anyone whose hands are wet, floury, or busy. The phone sits propped on the counter; they glance at it from arm's length or across the kitchen while chopping, stirring, or searing.

## Product Purpose

Tutti schedules a menu backwards from serving time and turns that schedule into live, generated music. Each dish is voiced by its own instrument and leitmotif, and the music tells the cook when to act, so every dish is ready together. Success is every dish reaching the table at the same moment, cued by ear, with no juggling of timers. Built for TXST Shipaton 2026, whose prompt asks for two categories that depend on each other; the pairing is Food × Music.

## Positioning

The recipe writes the music, and the music runs the kitchen. The schedule compiles into a score: steps become passages, dishes become instruments, and the action sets the tempo. The score conducts the cook: an instrument entering means a dish needs you, a building leitmotif means the moment is near, and the final chord means serve.

## Operating Context

- Four surfaces: Programme (choose dishes), Score (the schedule as a timeline, leitmotif previews, cue sheet), Conduct (the live stage: countdown, the current hands-on step, beat, per-dish status, upcoming cues, Done and +1 minute), and Finale (summary and encore).
- Used in a working kitchen with steam, noise, and messy hands. People glance rather than read; audio is the primary channel and the screen confirms it.
- Rehearsal mode runs a whole performance at 20× for demos.
- Hackathon deliverables: a public GitHub repo and an Adobe Express deck with screenshots and a demo video (sources in `docs/`).

## Capabilities and Constraints

- Native Android: Kotlin and Jetpack Compose (Compose BOM 2024.12.01), minSdk 26, portrait only.
- Scheduler (backward placement over a single hands-on lane, hold tolerances, hill-climb repair, live re-scoring), covered by unit tests over all 31 menus. The redesign must not change its behavior.
- Real-time synthesizer and sequencer on an AudioTrack thread, exposing a beat clock to the UI.
- Five built-in recipes, each with a dish color and a four-note pentatonic leitmotif: Garlic-Butter Salmon (cello), Lemon-Herb Rice (harp), Charred Broccoli (marimba), Little Gem Caesar (flute), Molten Mug Cake (celesta).
- Terminology: leitmotif, cue, hands-on versus heat, final chord ("tutti"), rehearsal, re-score, encore. Tempo markings map to actions: Allegro chop, Presto whisk, Andante stir, Vivace sear, Adagio simmer, Largo bake, Fermata rest.
- No image generation in this environment and no food photography assets.

## Brand Commitments

- Name: Tutti ("all together"). Current tagline: "Cook in concert."
- The user asked for a complete frontend redesign because the current look does not look nice. The concert-programme paper look, its fonts, and its launcher icon are not commitments.

## Evidence on Hand

- Working APK, unit tests, a demo video (`docs/demo/tutti-demo.mp4`), screenshots (`docs/screenshots/`), and a pitch deck (`docs/deck/`). All of these show the pre-redesign look and must be regenerated after the redesign.
- No user research, testimonials, metrics, or usage data exist. Do not invent them.

## Product Principles

1. Ears first, eyes second: every cue works as sound, and the screen confirms at a glance.
2. One pair of hands: never ask for two hands-on tasks at once; always show exactly what needs the cook now.
3. Readable from the counter: the primary state reads at arm's length in a busy kitchen.
4. Honest time: re-scoring is visible, and the app says when the final chord moves.
5. Delight belongs to moments (a cue, the final chord, the encore), not to every surface.

## Accessibility & Inclusion

- Audio-first with haptics. Dish identity never relies on color alone; instrument names and numerals accompany color.
- TalkBack labels and live-region announcements for cues and re-scores.
- Large type and 48dp minimum touch targets, usable with wet hands; no precise gestures required.
- Respect the system Remove animations setting.
