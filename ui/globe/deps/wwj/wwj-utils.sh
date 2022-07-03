#!/bin/bash

SCRIPT_PATH="$(cd "$(dirname $0)" && pwd)"

if [ $# -lt 1 ]; then
  WWJCLASS=gov.nasa.worldwindx.examples.NetworkOfflineMode
else
  WWJCLASS=$*
fi

cd "$SCRIPT_PATH"

echo Running ${WWJCLASS}
java --add-exports java.base/java.lang=ALL-UNNAMED --add-exports java.desktop/sun.awt=ALL-UNNAMED --add-exports java.desktop/sun.java2d=ALL-UNNAMED -Xmx2048m -cp ./worldwind-2.2.0.jar ${WWJCLASS}
