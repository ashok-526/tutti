#!/bin/bash
# Drive the Tutti UI by visible text through uiautomator.
#   ui.sh wait "Text" [seconds]   wait until a node containing Text is on screen
#   ui.sh tap  "Text"             tap the centre of the first node containing Text
ADB=~/Library/Android/sdk/platform-tools/adb

node_bounds() {
  $ADB shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
  $ADB shell cat /sdcard/ui.xml | tr '>' '\n' | grep -F "$1" \
    | grep -o 'bounds="\[[0-9]*,[0-9]*\]\[[0-9]*,[0-9]*\]"' | head -1 | tr -c '0-9\n' ' '
}

case "$1" in
  wait)
    for _ in $(seq 1 "${3:-40}"); do
      if [ -n "$(node_bounds "$2")" ]; then echo "found: $2"; exit 0; fi
      $ADB shell sleep 1
    done
    echo "timeout: $2"; exit 1 ;;
  tap)
    b=$(node_bounds "$2")
    if [ -z "$b" ]; then echo "missing: $2"; exit 1; fi
    set -- $b
    x=$(( ($1 + $3) / 2 )); y=$(( ($2 + $4) / 2 ))
    $ADB shell input tap "$x" "$y"
    echo "tapped @ $x,$y" ;;
esac
