#!/bin/bash
# Record the demo video: emulator screen + the app's own audio, aligned on the count-in.
ADB=~/Library/Android/sdk/platform-tools/adb
S=${TUTTI_WORK:-$HOME/tutti-work}
OUT=/Users/ashok/Desktop/tutti/docs/demo
UI="$(cd "$(dirname "$0")" && pwd)/ui.sh"
mkdir -p "$OUT" "$S"
tap() { $ADB shell input tap "$1" "$2"; }
nap() { $ADB shell sleep "$1"; }

$ADB shell am force-stop app.tutti
$ADB shell rm -f /sdcard/tutti_demo.mp4 /sdcard/Android/data/app.tutti/files/session.wav
$ADB logcat -c
$ADB shell am start -n app.tutti/.MainActivity --ez record true >/dev/null
bash "$UI" wait "Press the record" 60
nap 2

REC_START=$($ADB shell date +%s%3N)
# 720p keeps the emulator's encoder from starving the audio thread.
$ADB shell screenrecord --size 720x1600 --bit-rate 4000000 --time-limit 180 /sdcard/tutti_demo.mp4 &
REC_PID=$!
nap 2.5
$ADB shell input swipe 540 1700 540 1250 450
nap 1.4
tap 805 2222                  # Press the record
nap 2.6
$ADB shell input swipe 540 1900 540 850 600
nap 1.2
tap 540 764;  nap 2.0         # Cello theme
tap 540 923;  nap 2.0         # Harp theme
tap 540 1082; nap 2.4         # Marimba theme
tap 352 2158; nap 0.8         # Rehearsal
tap 771 2186                  # Drop the needle
T0=$(date +%s)
until [ $(( $(date +%s) - T0 )) -ge 28 ]; do nap 0.4; done
tap 795 2232                  # Done (finished rinsing early)
until [ $(( $(date +%s) - T0 )) -ge 38 ]; do nap 0.4; done
tap 283 2232                  # +1 minute
until [ $(( $(date +%s) - T0 )) -ge 124 ]; do nap 0.5; done
nap 1.5
$ADB shell input swipe 540 1900 540 600 600
nap 1.0
bash "$UI" tap "Play the encore" || tap 540 2100
nap 13
$ADB shell pkill -INT screenrecord
wait $REC_PID
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
