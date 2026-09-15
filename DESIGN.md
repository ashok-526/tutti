---
name: Tutti
description: Cook in concert. Dinner pressed onto one record, played until every dish is ready together.
colors:
  plinth: "#E3E6E9"
  surface: "#F0F2F4"
  sunken: "#CCD1D6"
  ink: "#15171A"
  ink-muted: "#4D535A"
  ink-faint: "#737A82"
  line: "#C3C9CF"
  on-ink: "#F0F2F4"
  vinyl: "#111214"
  groove: "#2A2C31"
  rim: "#3A3D42"
  label: "#F3F4F5"
  arm: "#2B2F35"
  arm-highlight: "#9AA1A9"
  cue: "#FFB21F"
  on-cue: "#15171A"
  cue-edge: "#B87900"
  plinth-dark: "#15171A"
  surface-dark: "#1F2226"
  sunken-dark: "#0B0C0E"
  ink-dark: "#ECEEF0"
  ink-muted-dark: "#A7ADB4"
  ink-faint-dark: "#858C94"
  line-dark: "#2E3238"
  on-ink-dark: "#15171A"
  vinyl-dark: "#08090A"
  groove-dark: "#1D1E21"
  rim-dark: "#44474D"
  label-dark: "#26292D"
  arm-dark: "#B9BFC6"
  arm-highlight-dark: "#E6E9EC"
  cue-edge-dark: "#FFCF70"
  dish-cello: "#E0481F"
  dish-harp: "#3B4CC0"
  dish-marimba: "#178A6E"
  dish-flute: "#D33D84"
  dish-celesta: "#8A55C9"
typography:
  numeral:
    fontFamily: "Archivo"
    fontSize: "84sp"
    fontWeight: 900
    lineHeight: "80sp"
    fontVariation: "'wdth' 62, 'wght' 900"
    fontFeature: "tnum"
  display:
    fontFamily: "Archivo"
    fontSize: "58sp"
    fontWeight: 800
    lineHeight: "56sp"
    letterSpacing: "-0.005em"
    fontVariation: "'wdth' 62, 'wght' 800"
  headline-stage:
    fontFamily: "Archivo"
    fontSize: "34sp"
    fontWeight: 700
    lineHeight: "38sp"
    letterSpacing: "-0.02em"
    fontVariation: "'wdth' 100, 'wght' 700"
  headline:
    fontFamily: "Archivo"
    fontSize: "32sp"
    fontWeight: 700
    lineHeight: "36sp"
    letterSpacing: "-0.02em"
    fontVariation: "'wdth' 100, 'wght' 700"
  time:
    fontFamily: "Archivo"
    fontSize: "22sp"
    fontWeight: 800
    lineHeight: "24sp"
    fontVariation: "'wdth' 62, 'wght' 800"
    fontFeature: "tnum"
  title:
    fontFamily: "Archivo"
    fontSize: "19sp"
    fontWeight: 600
    lineHeight: "24sp"
    letterSpacing: "-0.005em"
    fontVariation: "'wdth' 100, 'wght' 600"
  body:
    fontFamily: "Archivo"
    fontSize: "16sp"
    fontWeight: 400
    lineHeight: "24sp"
    fontVariation: "'wdth' 100, 'wght' 400"
  button:
    fontFamily: "Archivo"
    fontSize: "16sp"
    fontWeight: 600
    lineHeight: "20sp"
    fontVariation: "'wdth' 100, 'wght' 600"
  body-small:
    fontFamily: "Archivo"
    fontSize: "14sp"
    fontWeight: 400
    lineHeight: "20sp"
    fontVariation: "'wdth' 100, 'wght' 400"
  wordmark:
    fontFamily: "Archivo"
    fontSize: "15sp"
    fontWeight: 600
    lineHeight: "16sp"
    letterSpacing: "0.14em"
    fontVariation: "'wdth' 125, 'wght' 600"
  label:
    fontFamily: "Archivo"
    fontSize: "11sp"
    fontWeight: 600
    lineHeight: "16sp"
    letterSpacing: "0.08em"
    fontVariation: "'wdth' 125, 'wght' 600"
rounded:
  sleeve: "4dp"
  row: "16dp"
  pill: "50%"
spacing:
  xs: "4dp"
  sm: "8dp"
  md: "12dp"
  lg: "16dp"
  xl: "24dp"
  block: "28dp"
  section: "40dp"
components:
  button-primary:
    backgroundColor: "{colors.ink}"
    textColor: "{colors.on-ink}"
    typography: "{typography.button}"
    rounded: "{rounded.pill}"
    padding: "16dp 28dp"
    height: "56dp"
  button-primary-disabled:
    backgroundColor: "{colors.sunken}"
    textColor: "{colors.ink-muted}"
    rounded: "{rounded.pill}"
  button-signal:
    backgroundColor: "{colors.cue}"
    textColor: "{colors.on-cue}"
    typography: "{typography.button}"
    rounded: "{rounded.pill}"
    padding: "16dp 28dp"
    height: "56dp"
  button-secondary:
    textColor: "{colors.ink}"
    typography: "{typography.button}"
    rounded: "{rounded.pill}"
    padding: "16dp 24dp"
    height: "56dp"
  glyph-button:
    textColor: "{colors.ink}"
    size: "48dp"
  tag:
    backgroundColor: "{colors.sunken}"
    textColor: "{colors.ink-muted}"
    typography: "{typography.label}"
    rounded: "{rounded.pill}"
    padding: "5dp 10dp"
  tag-emphasis:
    backgroundColor: "{colors.sunken}"
    textColor: "{colors.ink}"
    typography: "{typography.label}"
    rounded: "{rounded.pill}"
    padding: "5dp 10dp"
  crate-row:
    rounded: "{rounded.row}"
    padding: "10dp 12dp"
  track-badge-chosen:
    backgroundColor: "{colors.ink}"
    textColor: "{colors.on-ink}"
    typography: "{typography.label}"
    rounded: "{rounded.pill}"
    size: "40dp"
  track-badge-unchosen:
    textColor: "{colors.ink-muted}"
    rounded: "{rounded.pill}"
    size: "40dp"
  sleeve:
    rounded: "{rounded.sleeve}"
    size: "60dp"
  audition-row:
    rounded: "{rounded.row}"
    padding: "12dp 12dp"
  readings-card:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.row}"
    padding: "8dp 20dp"
  dish-state-hands:
    backgroundColor: "{colors.cue}"
    textColor: "{colors.on-cue}"
    rounded: "{rounded.pill}"
    padding: "5dp 12dp"
  dish-state-cooking:
    backgroundColor: "{colors.sunken}"
    textColor: "{colors.ink}"
    rounded: "{rounded.pill}"
    padding: "5dp 12dp"
  dish-state-waiting:
    textColor: "{colors.ink-muted}"
    rounded: "{rounded.pill}"
    padding: "5dp 12dp"
  dish-state-ready:
    backgroundColor: "{colors.ink}"
    textColor: "{colors.on-ink}"
    rounded: "{rounded.pill}"
    padding: "5dp 12dp"
  snackbar:
    backgroundColor: "{colors.ink}"
    textColor: "{colors.on-ink}"
    typography: "{typography.button}"
    rounded: "{rounded.pill}"
---

# Design System: Tutti

## Overview

**Creative North Star: "The Turntable on the Counter"**

Dinner is a record you play. Every surface of Tutti is part of one modern deck: a flat aluminum-grey plinth (smoked graphite in the dark theme), a matte black disc pressed with one groove band per dish, a graphite tonearm that tracks inward through the cues, and a single amber cue light that means your hands, now. Time runs inward from the rim to the label, so the needle reaching the label means serve.

The interface is quiet, cool, and precise so the few moments that matter can be loud: a cue lighting the arm, a label swapping to a dish's ink, the record slowing to a stop at the finale. Color lives only in printed things (sleeves, labels, groove bands, label marks), and chrome stays in two inks on the plinth. Type is one family, Archivo, pulled to three widths: condensed heavy numerals you can read from across a kitchen, a normal width for instructions, and expanded caps for the lettering printed on labels.

Density is low and thumb-led. Each screen puts the disc at the top, one large statement under it, rows of dishes below, and the committing pill pinned at the bottom where a wet thumb reaches it. The world rejects recipe cards stacked with countdown timers and the album-art-plus-play-button music screen.

**Key Characteristics:**
- One flat plinth ground; depth only from objects that sit on it (disc, arm, sleeves).
- Two chrome inks (ink and ink-muted) plus one amber signal; dish inks printed only on record parts.
- Archivo at three widths: condensed 62, normal 100, expanded 125.
- Circles and pills for controls, 16dp soft rows, 4dp square sleeves.
- Physical motion: damped springs, strong ease-out for UI, linear rotation locked to the beat clock.
- Full light and dark themes that follow the system; Remove animations is honored.

## Colors

A cool, near-monochrome deck of aluminum greys and graphite, broken only by printed dish inks and one amber light.

### Primary
- **Graphite Ink** (ink): All primary text, the default pill fill, the chosen track badge, the ready-dish pill, the snackbar, and the strobe dots. In dark theme it inverts to Pale Aluminum (ink-dark) and the pill becomes light-on-dark.

### Secondary
- **Cue Amber** (cue): The one signal color, identical in both themes. It lights the tonearm's cue lamp, fills the Done pill, and fills the dish-state pill of the dish that needs your hands. Its darker Cue Rim (cue-edge; a lighter rim, cue-edge-dark, in dark theme) outlines it where it sits on a light ground. Text on amber is always Graphite Ink (on-cue).

### Tertiary
- **Salmon Red** (dish-cello): Garlic-Butter Salmon, voiced by the cello. Also the label color of the launcher icon record.
- **Deep Harp Blue** (dish-harp): Lemon-Herb Rice, voiced by the harp.
- **Marimba Green** (dish-marimba): Charred Broccoli, voiced by the marimba.
- **Flute Rose** (dish-flute): Little Gem Caesar, voiced by the flute.
- **Celesta Violet** (dish-celesta): Molten Mug Cake, voiced by the celesta.

Dish inks are printed, never painted: they appear as sleeve fills, groove bands (solid for hands-on steps, fine 50% grooves over a 7% wash for heat steps), label marks, the motif contour, and the Conduct label paper when that dish needs you.

### Neutral
- **Aluminum Plinth** (plinth / plinth-dark): The ground of every screen, the window background in `values/` and `values-night/`, the status-bar scrim, the spindle hole in label marks, and the launcher icon ground.
- **Brushed Panel** (surface / surface-dark): Raised panels on the plinth: the Finale readings card and the tonearm pivot cap.
- **Recessed Well** (sunken / sunken-dark): Inset fills: tags, the cooking-dish pill, the platter ring under the disc, disabled pills, the unchecked switch track.
- **Muted Ink** (ink-muted / ink-muted-dark): Supporting text, captions, instrument names, unchosen crate titles, the LIVE / REHEARSAL status, disabled pill text.
- **Faint Ink** (ink-faint / ink-faint-dark): Non-text outlines only (the unchecked switch border, the Material outline role). Never body text.
- **Hairline** (line / line-dark): The waiting-dish outline, the unchosen track-badge ring, the platter edge, the pivot cap edge.
- **Vinyl, Groove, Rim** (vinyl, groove, rim and their dark pairs): The disc body, its 0.7dp grooves every 3dp, and the 1dp rim lines at the lead-in, program edge, and label edge.
- **Label Paper** (label / label-dark): The resting record label and the sleeve record's cue mark.
- **Tonearm Graphite** (arm / arm-dark) and **Arm Glint** (arm-highlight / arm-highlight-dark): The arm, headshell, and pivot, with a 1dp glint along the tube and the unlit cue lamp at 40%.

### Named Rules
**The Cue Light Rule.** Amber means "your hands, now" and nothing else: the lit cue lamp, the Done pill, and the hands-state dish pill. Never use it for decoration, selection, links, or brand emphasis.

**The Printed Ink Rule.** Dish inks appear only on record parts (sleeves, bands, labels, label marks, motif contours). Plinth, panels, chrome, and text stay in the neutral inks.

**The Named Ink Rule.** A dish's color is never its only identifier. Every label mark, sleeve, and band sits beside the instrument name, the dish name, or its track number (A1 to A5).

**The Two Themes Rule.** Every token has a light and a dark value chosen from the system setting; cue amber is the only color shared by both. Dynamic color is not used.

## Typography

**Display Font:** Archivo variable, condensed width (wdth 62)
**Body Font:** Archivo variable, normal width (wdth 100)
**Label Font:** Archivo variable, expanded width (wdth 125)

**Character:** One grotesque stretched three ways, like the markings on a deck: heavy condensed figures you read across the room, plain instructions you read at arm's length, and wide spaced caps printed small on the label.

### Hierarchy
- **Numeral** (condensed 900, 84sp / 80sp, tabular): The Conduct countdown only, in its own slot beside the instruction.
- **Display** (condensed 800, 58sp / 56sp): One screen statement under the disc ("Tonight's record", "Serve at 7:42 PM", "Every dish, together.").
- **Headline Stage** (normal 700, 34sp / 38sp): The current instruction on Conduct, up to two lines, announced as a live region.
- **Headline** (normal 700, 32sp / 36sp): Section heads ("Hear each dish", "Tracklist", "Flip it over") and Conduct moments.
- **Time** (condensed 800, 22sp / 24sp, tabular): Cue times in the tracklist, Finale readings, the tempo marking, and the numerals printed on disc labels (scaled to the label, max 60sp).
- **Title** (normal 600, 19sp / 24sp): Row titles (dish names, step titles) and the Programme summary.
- **Body** (normal 400, 16sp / 24sp): Instructions and explanations, capped at 360dp wide.
- **Button** (normal 600, 16sp / 20sp): Pill text, dish instrument names on Conduct, switch label; the dish-state pill uses it at 15sp with tabular figures.
- **Body Small** (normal 400, 14sp / 20sp): Secondary row lines (instrument, minutes, marking).
- **Wordmark** (expanded 600, 15sp, 0.14em): "Tutti" at the top left of Programme and Finale.
- **Label** (expanded 600, 11sp, 0.08em, uppercase): Tags, track badges, the LIVE / REHEARSAL status, and sub-lettering on disc labels. Curved label lettering uses the same expanded 600 at 0.14em, 7 to 12sp, at 78% of the label ink.

### Named Rules
**The Three Widths Rule.** Condensed is for figures and the one display statement; normal is for anything read as a sentence; expanded is for short uppercase lettering only. Never set a sentence in condensed or expanded.

**The Tabular Time Rule.** Tabular figures are for time and nothing else: the countdown, cue times, readings, and dish-state pills. Numbers that change while watched never shift width.

**The sp Rule.** Every text size is in sp so it follows the system font scale; at font scale above 1.15 the Programme summary and its pill stack instead of sitting side by side.

## Layout

Single-column portrait on phones, edge to edge under transparent system bars. Screens use a 24dp text gutter; tappable rows sit in a 12dp outer inset with 12dp inner padding so their press surface reaches wider than the text column. Spacing steps on a 4dp base: 4 and 8 inside components, 12 between a statement and its body, 28 between a statement block and its list, 40 between sections.

Each screen follows one order: the disc at the top (bleeding off the top right on Programme at 380dp tall; centered-left with the arm on Conduct at 316dp; square on Score; 372dp on Finale), one display or headline statement, the list, and a bottom bar. Bottom bars float over scrolling content on a plinth gradient (transparent to solid by 30%), swallow taps so rows beneath never fire, and pad 36dp top and 16dp bottom above the navigation bar. Scrolling screens (Programme, Score, Finale) carry a plinth-colored status-bar scrim so the clock stays legible over content and vinyl.

Touch targets are at least 48dp; primary pills are 56dp tall and, when paired, split the width equally with a 12dp gap (secondary left, primary right).

### Named Rules
**The Still Instruction Rule.** On Conduct the countdown sits in its own right-aligned slot beside the instruction, so the text being read never moves as the numbers change.

**The Thumb Bar Rule.** The one committing action on each screen ("Press the record", "Drop the needle", Done) is a pill pinned at the bottom, never at the top.

## Elevation & Depth

The interface is flat: plinth, panels, and chrome carry no shadow and separate by tone (surface lifts, sunken recesses). Depth belongs only to physical objects that sit on the plinth, and those shadows are soft radial falloffs, solid under the object to 80% of their radius and fading to nothing at the edge. Dark theme deepens shadow opacity rather than lightening it. Light on the vinyl is a fixed sweep sheen (9% and 6% white highlights) that stays still while the record turns under it.

### Shadow Vocabulary
- **Disc shadow** (radial falloff, radius 1.12 of the disc, offset down 4% of radius, black 22% light / 60% dark): Under every record.
- **Pivot shadow** (radial falloff, 26dp radius, offset 2dp by 4dp, black 16% light / 50% dark): Under the tonearm pivot.
- **Sleeve lift** (Compose shadow elevation 1dp at rest, 6dp when chosen, 4dp corners): The only Material elevation in the app; the chosen sleeve rises as its record slides out.

### Named Rules
**The Objects Cast Rule.** Only the disc, the tonearm, and sleeves cast shadows. Pills, rows, cards, and tags stay flat.

**The Falloff Rule.** Shadows fade from a solid core to transparent. Never a hard offset copy of the shape.

**The Flat Plinth Rule.** The plinth is a flat fill. No brushed-metal texture, noise, or gradient on the ground; the only gradient on it is the bottom-bar fade back into plinth.

## Shapes

Circles and pills; sleeves are squares. The record, label marks, strobe dots, beat pips, the pivot, and track badges are true circles. Every button, tag, dish-state pill, and the snackbar is a full pill (50% corners). Tappable rows and the one card clip to 16dp corners, soft enough to sit with pills without becoming pills. Sleeves are nearly square-cornered at 4dp, like cardboard. Outlines are hairlines: 1dp on tags, dish-state pills, and platter edges; 1.5dp on the secondary pill and track badges.

### Named Rules
**The Three Radii Rule.** Pill for controls, 16dp for rows and cards, 4dp for sleeves. No other corner values.

## Components

### Buttons
Heavy, rounded, and sure-footed: they sink slightly when pressed, like a deck key.
- **Shape:** Full pill (50%), minimum 56dp tall.
- **Primary:** Graphite Ink fill, on-ink text in the button style, 28dp by 16dp padding. Used for "Press the record", "Drop the needle", "Play the encore".
- **Signal:** Cue Amber fill with Graphite Ink text. Only the Done pill on Conduct.
- **Secondary:** Transparent with a 1.5dp outline in ink at 50%, ink text, 24dp by 16dp padding ("+1 minute", "New menu").
- **Disabled:** Recessed Well fill with muted ink text; the secondary outline drops to Hairline.
- **Press:** Scale to 0.97 in 90ms, release in 180ms on the strong ease-out, plus the Material ripple.
- **Glyph buttons:** 48dp touch area, 24dp Material Symbols vector glyphs (back, close, volume on/off) in ink, always with a content description.

### Chips (Tags)
- **Style:** Pill in Recessed Well with 11sp expanded uppercase label text, 10dp by 5dp padding.
- **State:** Emphasis uses ink text (Hands); default uses muted ink (Heat). A cue variant (amber with cue-edge rim) exists for "your hands, now" only.

### Cards / Containers
- **Corner Style:** 16dp.
- **Background:** Brushed Panel (surface) on the plinth. The Finale readings card is the only card: label left in muted body text, value right in tabular time.
- **Shadow Strategy:** None; tonal lift only.
- **Border:** None.
- **Internal Padding:** 20dp horizontal, 8dp vertical, rows 12dp vertical.

### Crate Rows (Programme)
- **Structure:** Sleeve, dish name (title) over "instrument, minutes" (body small), and a 40dp track badge on the right, in a 16dp-clipped toggle row with checkbox semantics.
- **Chosen:** Title in ink; badge fills ink with the track number (A1 to A5) in label caps; the record slides half out of its sleeve on a spring (damping 0.8, stiffness 380), turning 70 degrees.
- **Unchosen:** Only the sleeve fades, to 58% over 200ms; the title drops to muted ink, the badge becomes a Hairline ring with a muted add glyph. Text never fades below its muted ink.

### Dish State Lines (Conduct)
A dish's state is told four ways, never by color alone: label mark, instrument name, a one-line detail, and a pill whose fill, border, and wording all change.
- **Needs you:** Cue Amber pill with cue-edge rim, "hands".
- **Cooking:** Recessed Well pill, ink text, remaining time.
- **Waiting:** Transparent pill with Hairline border, muted "in 3:10".
- **Ready (holding):** Graphite Ink pill, on-ink "ready".

### Navigation
There is no navigation bar. Screens advance linearly (Programme, Score, Conduct, Finale) with a back or close glyph at the top left and system back. Screen changes fade in over 240ms with a scale from 0.97 over 280ms (80ms delay) on the strong ease-out, while the old screen fades out in 90ms; with Remove animations on, screens swap instantly.

### Snackbar
A Graphite Ink pill with on-ink button text, floating 96dp above the bottom bar, announced politely to TalkBack. Used for re-score notices ("The final chord moves 0:30 later."), shown for 3.6s.

### Turntable (Signature Component)
One Canvas record, reused on every screen at different sizes and crops.
- **Pressing:** Vinyl with fine grooves; each dish owns an equal slice (4 degree gaps); its steps are pressed as bands from the rim (downbeat) toward the label (final chord) on an equal-area radius, so every minute gets the same amount of vinyl.
- **Label:** 36% of radius, printed in Label Paper with curved expanded caps top and bottom and an upright tabular numeral with a label-caps sub-line. On Conduct the paper cross-fades in 220ms to the ink of the dish that needs you; label text switches to near-black or near-white by the paper's luminance.
- **Live state:** A 1.4dp white ring marks now; the played area dims under 62% vinyl; the platter ring's 48 strobe dots pulse on each beat; rotation is locked linearly to the beat clock (45 degrees per beat); a cue flashes its slice with 30% white decaying over 1.4s.
- **Tonearm:** Graphite tube with glint, headshell, and a cue lamp that lights amber when your hands are needed. The arm follows the session critically damped (0.3s time constant), lowers onto the record over 2.2s, and lifts after the final chord.
- **Finale:** The record spins down 70 degrees over 2.6s on the strong ease-out, and the Finale disc settles 150 degrees over 2.2s as the arm lifts.
- **Reduced motion:** With Remove animations on, the disc does not spin, the needle drops and lifts instantly, and the Finale disc starts settled.

### Label Mark, Sleeve, Motif Contour
- **Label Mark:** A dish's identity dot: a filled ink circle with a plinth spindle hole at 13% (14 to 28dp), always beside a name.
- **Sleeve:** A 60dp square in dish ink with 4dp corners and a faint printed ring, holding a 54dp record with a dish-ink label.
- **Motif Contour:** A 56 by 28dp four-note pitch line in dish ink (45% segments); the sounding note grows from 3.4 to 5dp while auditioned.

## Do's and Don'ts

### Do:
- **Do** set every screen on the flat Aluminum Plinth and let the disc, arm, and sleeves be the only objects with depth.
- **Do** reserve Cue Amber (#FFB21F) for the cue lamp, the Done pill, and the hands-state pill.
- **Do** pair every dish ink with its instrument name, dish name, or track number.
- **Do** use Archivo condensed for figures and the one display statement, normal for sentences, and expanded for short uppercase label lettering.
- **Do** use tabular figures for every time value, and keep the countdown in its own slot so the instruction never moves.
- **Do** keep to three radii: pills for controls, 16dp for rows and cards, 4dp for sleeves.
- **Do** make every shadow a soft radial falloff that deepens in dark theme.
- **Do** provide light and dark values for every new color and check `animationsEnabled()` before any decorative or long-running motion.
- **Do** keep touch targets at 48dp or more and primary pills at 56dp, pinned at the thumb.

### Don't:
- **Don't** add brushed-metal texture, noise, or gradients to the plinth.
- **Don't** use amber for selection, links, highlights, or brand color.
- **Don't** paint dish inks onto panels, chrome, or text; they are printed on record parts only.
- **Don't** let color alone tell dishes or dish states apart.
- **Don't** use hard offset shadows or Material elevation on pills, rows, or cards.
- **Don't** put small uppercase tags or eyebrows above headings; a statement stands on its own.
- **Don't** set sentences in condensed or expanded widths, or use fixed px for text.
- **Don't** enable dynamic color; the deck's inks replace the wallpaper palette.
