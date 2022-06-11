#!/bin/bash

# TODO: JAVA_OPTS, Xmx/Xms

exec java --add-exports java.base/java.lang=ALL-UNNAMED --add-exports java.desktop/sun.awt=ALL-UNNAMED --add-exports java.desktop/sun.java2d=ALL-UNNAMED -Xmx4096m -Xms4096m -cp *:deps/* asaintsever.tinyworld.ui.UI