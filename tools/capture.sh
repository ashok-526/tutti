#!/bin/bash
# Inspection round for the turntable redesign: light flow end to end, dark theme, 1.3x font scale.
ADB=~/Library/Android/sdk/platform-tools/adb
S=${TUTTI_WORK:-$HOME/tutti-work}
R=$S/r2
UI="$(cd "$(dirname "$0")" && pwd)/ui.sh"
mkdir -p "$R"
shot() { $ADB exec-out screencap -p > "$R/$1.png"; }
nap() { $ADB shell sleep "$1"; }
since() { echo $(( $(date +%s) - T0 )); }
until_t() { until [ "$(since)" -ge "$1" ]; do nap 0.4; done; }
launch() {
  $ADB shell am force-stop app.tutti
  $ADB shell am start -n app.tutti/.MainActivity >/dev/null
  bash "$UI" wait "Press the record" 60
  nap 1.5
}
to_stage() {
  bash "$UI" tap "Press the record" || $ADB shell input tap 800 2230
  bash "$UI" wait "Drop the needle" 20
  nap 1.2
}

$ADB shell cmd uimode night no
$ADB shell settings put system font_scale 1.0
$ADB install -r /Users/ashok/Desktop/tutti/app/build/outputs/apk/release/app-release.apk | tail -1
$ADB logcat -c

# Light: programme, score, conduct, finale
launch
shot p1
$ADB shell input swipe 540 1900 540 900 500; nap 1; shot p2
$ADB shell input swipe 540 900 540 1900 400; nap 0.6
to_stage
shot s1
$ADB shell input swipe 540 1900 540 700 500; nap 1; shot s2
$ADB shell input swipe 540 1900 540 500 500; nap 1; shot s3
bash "$UI" tap "Rehearsal" || $ADB shell input tap 220 2230
nap 0.8
bash "$UI" tap "Drop the needle" || $ADB shell input tap 800 2230
T0=$(date +%s)
nap 1.2; shot c0
until_t 26; shot c1
$ADB shell input tap 283 2232; nap 1.2; shot c2
until_t 44; shot c3
until_t 64; shot c4
until_t 116
for i in 1 2 3 4 5 6; do shot "f$i"; nap 2; done
nap 4; shot f7
$ADB shell input swipe 540 1900 540 700 500; nap 1.2; shot f8

# Dark theme
$ADB shell cmd uimode night yes
launch
shot d1
to_stage
shot d2
bash "$UI" tap "Rehearsal" || $ADB shell input tap 220 2230
nap 0.8
bash "$UI" tap "Drop the needle" || $ADB shell input tap 800 2230
T0=$(date +%s)
until_t 27; shot d3
$ADB shell am force-stop app.tutti
$ADB shell cmd uimode night no

# Font scale 1.3
$ADB shell settings put system font_scale 1.3
launch
shot x1
to_stage
bash "$UI" tap "Rehearsal" || $ADB shell input tap 220 2230
nap 0.8
bash "$UI" tap "Drop the needle" || $ADB shell input tap 800 2230
T0=$(date +%s)
until_t 27; shot x2
$ADB shell am force-stop app.tutti
$ADB shell settings put system font_scale 1.0

for f in "$R"/*.png; do sips -Z 900 "$f" --out "${f%.png}.v.jpg" -s format jpeg >/dev/null 2>&1; done
$ADB logcat -d | grep -E "FATAL EXCEPTION" -A 14 | head -30
echo done
