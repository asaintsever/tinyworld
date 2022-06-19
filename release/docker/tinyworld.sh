#!/bin/bash

exec java --add-exports java.base/java.lang=ALL-UNNAMED --add-exports java.desktop/sun.awt=ALL-UNNAMED --add-exports java.desktop/sun.java2d=ALL-UNNAMED ${JAVA_OPTS:--Xmx4096m -Xms4096m} -cp *:deps/* asaintsever.tinyworld.ui.UI