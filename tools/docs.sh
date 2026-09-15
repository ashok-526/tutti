#!/bin/bash
# Turn a capture set (from tools/capture.sh) into the docs: named screenshots, the hero image, and deck slides.
# Usage: bash tools/docs.sh <capture-dir>   e.g. bash tools/docs.sh ~/tutti-work/round2/r2
# Override frame choices when timing shifted: HANDS_FREE=c3 FINISH=f3 FINALE=f6 bash tools/docs.sh <dir>
set -e
SRC="${1:?capture directory}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/docs/screenshots"
mkdir -p "$OUT"

copy() { cp "$SRC/$1.png" "$OUT/$2.png"; echo "$1 -> $2"; }
copy p1 01-programme
copy s1 02-score
copy "${COUNT_IN:-c0}" 03-count-in
copy "${HANDS_ON:-c1}" 04-hands-on
copy "${RESCORED:-c2}" 05-rescored
copy "${HANDS_FREE:-c4}" 06-listen
copy "${FINISH:-f2}" 07-tutti
copy "${FINALE:-f7}" 08-finale
[ -f "$SRC/d1.png" ] && copy d1 09-dark-programme
[ -f "$SRC/d3.png" ] && copy d3 10-dark-stage

# Four screens side by side on the plinth color.
ffmpeg -hide_banner -loglevel error -y \
  -i "$OUT/01-programme.png" -i "$OUT/02-score.png" -i "$OUT/04-hands-on.png" -i "$OUT/08-finale.png" \
  -filter_complex "[0]scale=540:1200,pad=620:1280:40:40:color=0xE3E6E9[a];[1]scale=540:1200,pad=620:1280:40:40:color=0xE3E6E9[b];[2]scale=540:1200,pad=620:1280:40:40:color=0xE3E6E9[c];[3]scale=540:1200,pad=620:1280:40:40:color=0xE3E6E9[d];[a][b][c][d]hstack=inputs=4,pad=iw+80:ih+80:40:40:color=0xE3E6E9" \
  -frames:v 1 "$OUT/hero.png"
echo "hero.png"

bash "$ROOT/docs/deck/render.sh" | tail -1
