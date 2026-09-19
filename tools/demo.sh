#!/bin/bash
# Record the demo video: emulator screen + the app's own audio, aligned on the count-in.
ADB=~/Library/Android/sdk/platform-tools/adb
S=${TUTTI_WORK:-$HOME/tutti-work}
OUT=/Users/ashok/Desktop/tutti/docs/demo
UI="$(cd "$(dirname "$0")" && pwd)/ui.sh"
mkdir -p "$OUT" "$S"
tap() { $ADB shell input tap "$1" "$2"; }
nap() { $ADB shell sleep "$1"; }

$ADB wait-for-device
$ADB shell echo device-ready
$ADB shell am force-stop app.tutti
$ADB shell rm -f /sdcard/tutti_demo.mp4 /sdcard/Android/data/app.tutti/files/session.wav
$ADB logcat -c
# 35x rehearsal keeps the whole take under the two-minute limit.
$ADB shell am start -n app.tutti/.MainActivity --ez record true --ef rehearsalSpeed 35 >/dev/null
bash "$UI" wait "Press the record" 60
nap 2

REC_START=$($ADB shell date +%s%3N)
# Detached on the device, so an adb hiccup can't end the take early. 720p keeps the encoder light.
$ADB shell "nohup screenrecord --size 720x1600 --bit-rate 4000000 --time-limit 180 /sdcard/tutti_demo.mp4 >/dev/null 2>&1 &"
until $ADB shell ls /sdcard/tutti_demo.mp4 >/dev/null 2>&1; do nap 0.3; done
nap 1.5
$ADB shell input swipe 540 1700 540 1250 450
nap 1.4
tap 805 2222                  # Press the record
nap 2.6
$ADB shell input swipe 540 1900 540 850 600
nap 1.2
tap 540 764;  nap 1.8         # Cello theme
tap 540 923;  nap 1.8         # Harp theme
tap 540 1082; nap 2.2         # Marimba theme
tap 352 2158; nap 0.8         # Rehearsal
tap 771 2186                  # Drop the needle
# Offsets from the needle drop: a ~2.9 s count-in, then dinner time runs at 35x.
T0=$(date +%s)
until [ $(( $(date +%s) - T0 )) -ge 16 ]; do nap 0.4; done
tap 795 2232                  # Done (finished rinsing early)
until [ $(( $(date +%s) - T0 )) -ge 23 ]; do nap 0.4; done
tap 283 2232                  # +1 minute
until [ $(( $(date +%s) - T0 )) -ge 71 ]; do nap 0.5; done
nap 1.0
$ADB shell input swipe 540 1900 540 500 500
nap 0.6
$ADB shell input swipe 540 1900 540 500 500
nap 1.5
tap 900 1882                  # Tip the band: a coffee
nap 2.5
tap 749 1327                  # Test Store sheet: Test valid purchase
bash "$UI" wait "Thank you" 30
nap 6                         # the band answers with the final chord
$ADB shell pkill -INT screenrecord
while $ADB shell pidof screenrecord >/dev/null 2>&1; do nap 0.5; done
nap 2

$ADB pull /sdcard/tutti_demo.mp4 "$S/demo_raw.mp4" | tail -1
$ADB pull /sdcard/Android/data/app.tutti/files/session.wav "$S/session.wav" | tail -1
AUDIO=$($ADB logcat -d | grep "audio-start" | tail -1 | awk '{print $NF}')
COUNTIN=$($ADB logcat -d | grep "countin-start" | tail -1 | awk '{print $NF}')

# Sync point: the thumb-height controls turn from the black "Drop the needle" pill to the light,
# disabled Done pill when the stage appears. Find the first run of 1 s where that region stays light.
FLIP=$(ffmpeg -hide_banner -i "$S/demo_raw.mp4" -vf "fps=20,crop=280:40:373:1440,signalstats,metadata=print:key=lavfi.signalstats.YAVG" -an -f null - 2>&1 \
  | grep -oE "pts_time:[0-9.]+|YAVG=[0-9.]+" | paste - - \
  | awk '{t=substr($1,10); y=substr($2,6)+0; if (y > 120) { if (run == 0) start = t; run++; if (run >= 20) { print start; exit } } else run = 0 }')
if [ -n "$FLIP" ]; then
  # The new screen's fade crosses half-way about 0.2 s after the count-in starts.
  SHIFT=$(awk "BEGIN { printf \"%.3f\", $FLIP - 0.2 - ($COUNTIN - $AUDIO) / 1000 }")
  echo "sync: stage at ${FLIP}s"
else
  SHIFT=$(awk "BEGIN { printf \"%.3f\", ($AUDIO - $REC_START) / 1000 - 0.4 }")
  echo "sync: timestamps only"
fi
echo "audio=$AUDIO countin=$COUNTIN rec=$REC_START shift=${SHIFT}s"

if awk "BEGIN { exit !($SHIFT >= 0) }"; then
  AUDIO_OPTS=(-itsoffset "$SHIFT")
else
  AUDIO_OPTS=(-ss "$(awk "BEGIN { printf \"%.3f\", -($SHIFT) }")")
fi

ffmpeg -hide_banner -loglevel error -y \
  -i "$S/demo_raw.mp4" \
  "${AUDIO_OPTS[@]}" -f s16le -ar 48000 -ac 2 -skip_initial_bytes 44 -i "$S/session.wav" \
  -map 0:v -map 1:a -vf "scale=720:-2,fps=30" -c:v libx264 -preset veryfast -crf 26 -pix_fmt yuv420p \
  -c:a aac -b:a 160k -shortest -movflags +faststart "$OUT/tutti-demo.mp4"
ls -la "$OUT/tutti-demo.mp4" | awk '{print "demo bytes", $5}'
ffprobe -v error -show_entries format=duration -of csv=p=0 "$OUT/tutti-demo.mp4"
$ADB logcat -d | grep -E "FATAL EXCEPTION" -A 10 | head -20
echo done
