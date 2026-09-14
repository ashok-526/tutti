# Handoff: where the Tutti work stopped

For a fresh Claude Code session started in this folder. Start it with: *"Read HANDOFF.md and continue from where it stopped."*

## Deadline and deliverables

TXST Shipaton 2026. Submit by **11:59 PM CDT, Sep 14 2026**: a public GitHub repo, plus a public Adobe Express presentation with vision, impact, app screenshots, and a demo video.
Nothing has been pushed yet. Publishing is the user's call: `gh repo create tutti --public --source=. --push` (gh is logged in as `ashok-526`).

## What's done

- The app: native Android, Kotlin + Jetpack Compose. It has a backward scheduler with live re-scoring (`schedule/Conductor.kt`, with unit tests across all 31 menus, all passing), a from-scratch synth and sequencer (`audio/`), and four screens.
- Docs from the first version (README, `docs/PITCH.md`, screenshots, demo video, deck). All committed.
- **A full frontend redesign, in progress.** The user asked for it using the skills impeccable, emil-design-eng, design-taste-frontend (taste), and ui-ux-pro-max, plus the 21st.dev MCP.
  - `PRODUCT.md` was written, inferred without an interview because the user asked not to be asked questions.
  - Impeccable direction roll: seed `c38c542d`, assigned direction **"turntable pressing"**. The contract lives in `.impeccable/surfaces/app-src-main-java-app-tutti-ui.md` and must be followed.
  - New design system in `ui/Theme.kt`: an aluminum plinth (light), smoked graphite (dark), black vinyl, dish colors as label inks, and a single amber color meaning "your hands, now". The font is Archivo (variable) at condensed, normal, and expanded widths. Material 3 controls.
  - `ui/Record.kt`: the Canvas turntable. Each dish is a slice of the disc, time runs from the rim to the label (equal-area), with a tonearm on a damped spring, platter strobe dots, label text curved around the disc, and cue flashes.
  - All four screens were rewritten (`ProgrammeScreen`, `ScoreScreen`, `ConductScreen`, `FinaleScreen`), plus a new launcher icon (a record), dark theme resources, and the README/PITCH/deck copy updated for the turntable world.
  - Inspection round 1 (emulator screenshots) led to three fixes, applied: the stage's clipped horizontal dish chips became a vertical status list, the wordmark got bigger, and the heat grooves were toned down.

## What's left, in order

1. Rebuild, install, and **finish inspection**: review the dark-theme, 1.3× font-scale, and finale captures (`bash tools/capture.sh`, output in `$TUTTI_WORK/r2`). Fix what they show in one batch, then run one confirming round. Two rounds maximum.
2. **Impeccable finish**: spawn the `impeccable-finish-reviewer` agent. Pass it the request, the direction contract path above, the screenshot paths, `/Users/ashok/.claude/skills/impeccable/reference/craft-floor.md`, `.../reference/android.md`, and a note that no detector runs on native. Then act on its verdict (ship / fix / rebuild / recapture). Next, spawn `impeccable-documenter` to write `DESIGN.md` and `.impeccable/design.json` from the built app.
3. **Regenerate the docs** from the redesigned build:
   - Screenshots `docs/screenshots/01-programme.png` … `08-finale.png` (keep these names: count-in, hands-on, re-scored, hands free, finish moment, finale), plus `hero.png` (four screens side by side, generated with ffmpeg hstack).
   - Deck slides: `bash docs/deck/render.sh` (uses headless Brave; `deck.html` is already restyled).
   - Demo video: `bash tools/demo.sh` records the screen plus the app's own audio. It aligns them using the `audio-start` and `countin-start` log lines against the moment the screen goes dark at the count-in.
4. Commit, and remind the user about pushing and the Adobe Express deck.

## Environment notes

- There's no Xcode and disk space is tight (check with `df -h ~`). Android SDK: `~/Library/Android/sdk`, AVD `Medium_Phone_API_36.0`.
  Boot: `~/Library/Android/sdk/emulator/emulator -avd Medium_Phone_API_36.0 -no-snapshot-save -no-audio` (run it in the background).
- Build: `./gradlew :app:assembleRelease` (R8 release APK, fast on the emulator). Tests: `./gradlew :app:testDebugUnitTest`.
- The emulator has no audio device. To check audio, record the app's own output (`--ez record true` launch extra writes `session.wav` under `/sdcard/Android/data/app.tutti/files/`).
- `tools/ui.sh wait|tap "Text"` drives the UI by visible text through uiautomator (slow; use coordinates during animated screens).
- The tool scripts write to `$TUTTI_WORK` (default `~/tutti-work`).
