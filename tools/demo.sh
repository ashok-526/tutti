#!/bin/bash
# Record the demo video: emulator screen + the app's own audio, aligned on the count-in.
ADB=~/Library/Android/sdk/platform-tools/adb
S=${TUTTI_WORK:-$HOME/tutti-work}
OUT=/Users/ashok/Desktop/tutti/docs/demo
UI="$(cd "$(dirname "$0")" && pwd)/ui.sh"
mkdir -p "$OUT"
tap() { $ADB shell input tap "$1" "$2"; }
nap() { $ADB shell sleep "$1"; }

$ADB shell am force-stop app.tutti
$ADB shell rm -f /sdcard/tutti_demo.mp4 /sdcard/Android/data/app.tutti/files/session.wav
$ADB logcat -c
$ADB shell am start -n app.tutti/.MainActivity --ez record true >/dev/null
bash "$UI" wait "Compose" 60
nap 2

$ADB shell screenrecord --bit-rate 8000000 --time-limit 180 /sdcard/tutti_demo.mp4 &
REC_PID=$!
nap 2.5
$ADB shell input swipe 540 1700 540 1250 450
nap 1.2
tap 794 2225                  # Compose
nap 2.2
$ADB shell input swipe 540 1900 540 1000 550
nap 1.0
tap 227 786;  nap 1.8         # Cello
tap 227 995;  nap 1.8         # Harp
tap 278 1204; nap 2.2         # Marimba
tap 271 2207; nap 0.9         # Rehearsal
tap 738 2225                  # Raise the baton
T0=$(date +%s)
until [ $(( $(date +%s) - T0 )) -ge 28 ]; do nap 0.4; done
tap 800 2228                  # Done (finished rinsing early)
until [ $(( $(date +%s) - T0 )) -ge 38 ]; do nap 0.4; done
tap 288 2228                  # +1 minute
until [ $(( $(date +%s) - T0 )) -ge 124 ]; do nap 0.5; done
nap 1.5
$ADB shell input swipe 540 1900 540 600 600
nap 1.0
bash "$UI" tap "Play the encore" || tap 272 2162
nap 16
$ADB shell pkill -INT screenrecord
wait $REC_PID
nap 2

$ADB pull /sdcard/tutti_demo.mp4 "$S/demo_raw.mp4" | tail -1
$ADB pull /sdcard/Android/data/app.tutti/files/session.wav "$S/session.wav" | tail -1
SESSION=$($ADB logcat -d | grep "audio-start" | tail -1 | awk '{print $NF}')
COUNTIN=$($ADB logcat -d | grep "countin-start" | tail -1 | awk '{print $NF}')
DARK=$(ffmpeg -hide_banner -i "$S/demo_raw.mp4" -vf "fps=20,signalstats,metadata=print:key=lavfi.signalstats.YAVG" -an -f null - 2>&1 \
  | grep -oE "pts_time:[0-9.]+|YAVG=[0-9.]+" | paste - - \
  | awk '{t=substr($1,10); y=substr($2,6); if (y+0 < 80) { print t; exit }}')
# The stage crosses the brightness threshold ~0.44 s after the count-in begins (crossfade timing).
SHIFT=$(awk "BEGIN { printf \"%.3f\", $DARK - 0.44 - ($COUNTIN - $SESSION) / 1000 }")
echo "session=$SESSION countin=$COUNTIN dark=${DARK}s shift=${SHIFT}s"

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
