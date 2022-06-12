#!/bin/bash

# For AppImage: must CD into directory where our script is located to get classpath right
SCRIPT_PATH="$(cd "$(dirname $0)" && pwd)"
cd $SCRIPT_PATH

# TODO: add Java JRE 17 under AppDir/usr/bin, change java call to './jre/bin/java ...'

# TODO: JAVA_OPTS, Xmx/Xms

exec java --add-exports java.base/java.lang=ALL-UNNAMED --add-exports java.desktop/sun.awt=ALL-UNNAMED --add-exports java.desktop/sun.java2d=ALL-UNNAMED -Xmx4096m -Xms4096m -cp *:deps/* asaintsever.tinyworld.ui.UI