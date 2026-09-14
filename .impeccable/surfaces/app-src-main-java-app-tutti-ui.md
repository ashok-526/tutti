---
version: 1
slug: "app-src-main-java-app-tutti-ui"
primary_target: "app/src/main/java/app/tutti/ui"
related_targets: ["app/src/main/java/app/tutti/MainActivity.kt"]
---

# Tutti app surfaces

Scope: the whole Android app UI (Programme, Score, Conduct, Finale). Visitor mode: Operate. The cook is in a task; the stage must read at a glance from the counter.

Audience and job: a home cook plating two to five dishes at once, phone propped on the counter, hands busy. Job: follow cues by ear and glance until every dish is ready together.

Constraints: Material 3 structure and components on Android, themed into the world; scheduler and audio behavior unchanged; 48dp targets; TalkBack live regions kept; system dark theme supported; Remove animations honored.

Memorable moment: the needle reaching the label as the final chord lands, and the record slowing to a stop.

Unresolved: none blocking. Direction chosen unattended (the user asked not to be asked questions).

## Direction contract

THESIS: Dinner is a record you play. The menu is pressed onto one disc, each dish a groove band, the tonearm tracks inward through the cues, and the needle reaching the label means serve. It refuses recipe cards stacked with countdown timers, and the album-art-plus-play-button music screen.

OWN-WORLD: A modern turntable. Cool brushed-aluminum plinth ground (smoked graphite in dark theme), matte black vinyl with fine grooves and a slow sheen, dish inks printed only on labels and groove bands, a graphite tonearm, and one amber cue light that means your hands, now. Archivo in three widths: condensed heavy numerals, normal instructions, expanded label caps. Circles and pills; sleeves are squares.

STORY: The cook watches the menu become one record, drops the needle, follows the arm and the cue light by glance and ear, and serves when it reaches the label.

FIRST VIEWPORT: Programme: a large pressing disc bleeding off the top right, one band per chosen dish, label showing total minutes; crate rows of sleeve swatches below; a "Press the record" pill pinned at the thumb. Conduct: spinning disc across the top with the arm at now, a giant condensed countdown, the current instruction, Done and +1 minute pills at thumb height.

FORM: Turntable pressing, position 4 of 7 on the ordered list, seed c38c542d. Signature interaction: the arm tracks inward in real time while the platter's strobe dots pulse on the beat; when a cue fires, that dish's band lights and the label swaps to its ink. Motion grammar: damped springs for the arm and bands, 200ms ease-out for UI, linear rotation off the beat clock, a spin-down at the finale.
Raised from the declined hand: rank is depth (cracktro queue): now, next, and later differ by size and ink, and the instruction never moves while read. Physical, damped motion with value plus trend (night six-pack). Color only from dish inks; chosen lifts, unchosen recedes (streaming wall). A strict 8dp grid and one signal color (man-machine). One time axis rules every drawing; tabular digits only for time (deep dive). Four named dish states that differ beyond color: waiting, cooking, needs you, holding (tensegrity).

FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
