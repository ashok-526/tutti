#!/bin/bash
# Render each slide of deck.html to a 1920x1080 PNG (for importing into Adobe Express).
# Usage: bash docs/deck/render.sh [path-to-chromium-browser]
set -e
DIR="$(cd "$(dirname "$0")" && pwd)"
BROWSER="${1:-/Applications/Brave Browser.app/Contents/MacOS/Brave Browser}"
[ -x "$BROWSER" ] || BROWSER="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
mkdir -p "$DIR/slides"
for i in $(seq 1 12); do
  n=$(printf "%02d" "$i")
  "$BROWSER" --headless=new --disable-gpu --hide-scrollbars --force-device-scale-factor=1 \
    --window-size=1920,1080 --virtual-time-budget=4000 \
    --screenshot="$DIR/slides/$n.png" "file://$DIR/deck.html#$i" >/dev/null 2>&1
  echo "slide $n"
done
