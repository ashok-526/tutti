# Handoff: where the Tutti work stopped

For a fresh Claude Code session started in this folder. Start it with: *"Read HANDOFF.md and continue from where it stopped."*

## Deadline and deliverables

TXST Shipaton 2026. Submit by **11:59 PM CDT, Sep 14 2026**: a public GitHub repo, plus a public Adobe Express presentation with vision, impact, app screenshots, and a demo video.
**Nothing has been pushed yet.** Publishing is the user's call: `gh repo create tutti --public --source=. --push` (gh is logged in as `ashok-526`).

## What's done

- The app: native Android, Kotlin + Jetpack Compose + Material 3. It has a backward scheduler with live re-scoring (`schedule/Conductor.kt`, unit tests across all 31 menus passing), a from-scratch synth and sequencer (`audio/`), and four screens.
- **The turntable redesign is finished and reviewed.**
  - Impeccable direction seed `c38c542d`. The contract is in `.impeccable/surfaces/app-src-main-java-app-tutti-ui.md`.
  - The finish reviewer's first verdict was "fix" with 8 items. All 8 are resolved, and the final verdict is **ship**.
  - `DESIGN.md` and `.impeccable/design.json` document the built system.
  - Optional polish the reviewer listed as non-blocking: a bigger amber cue light, a 33/45-style rehearsal selector instead of the stock switch, a sleeve-back Finale summary, a notation-style theme preview, and printed sleeves.
- Docs are regenerated from the final build:
  - `docs/screenshots/01…10` and `hero.png`
  - `docs/deck/slides/01…12.png` (source `docs/deck/deck.html`; the closing slide uses `docs/deck/record.png`)
  - `docs/PITCH.md` and the README.
- Tools: `tools/capture.sh` (inspection screenshots), `tools/docs.sh <capture-dir>` (screenshots, hero, and slides; frame overrides via `HANDS_ON= HANDS_FREE= FINISH= FINALE=`), `tools/demo.sh` (demo video with the app's own audio), and `tools/ui.sh` (drive the UI by text).

## What's left

The demo video (`docs/demo/tutti-demo.mp4`) is done: a complete 2:36 take, verified with audio on the wall clock and the count-in clicks within 0.1 s of the stage flip. Earlier takes drifted because the emulator's audio sink ran slower than real time. `audio/Orchestra.kt` now keeps the music on the wall clock, and `tools/demo.sh` runs screenrecord detached.

1. Push the repo public (user's call).
2. The user builds the Adobe Express presentation from `docs/deck/slides/` and the demo video, makes it public, and submits both links.

## Environment notes

- There's no Xcode and disk space is tight. Android SDK: `~/Library/Android/sdk`, AVD `Medium_Phone_API_36.0`.
  Boot: `~/Library/Android/sdk/emulator/emulator -avd Medium_Phone_API_36.0 -no-snapshot-save -no-audio` (run it in the background).
- Build: `./gradlew :app:assembleRelease`. Tests: `./gradlew :app:testDebugUnitTest`.
- The emulator has no audio device. The `--ez record true` launch extra writes `session.wav` under `/sdcard/Android/data/app.tutti/files/`.
- The tool scripts write working files to `$TUTTI_WORK` (default `~/tutti-work`).
