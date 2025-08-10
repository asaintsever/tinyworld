#!/bin/bash

set -e

ARCH=$1
RELEASE_VERSION=$2

MAIN_JAR="ui-$RELEASE_VERSION.jar"
MAIN_CLASS="asaintsever.tinyworld.ui.UI"

# Strip any "-SNAPSHOT" suffix from the version as jpackage does not support it
if [[ $RELEASE_VERSION == *"-SNAPSHOT" ]]; then
  RELEASE_VERSION=${RELEASE_VERSION%-SNAPSHOT}
fi

# Check if version starts with "0."
if [[ $RELEASE_VERSION == 0.* ]]; then
  echo "Version starts with 0. This is not supported by jpackage: enforcing version to 1.0.0."
  RELEASE_VERSION="1.0.0"
fi

JVM_OPTIONS="--add-exports=java.base/java.lang=ALL-UNNAMED --add-exports=java.desktop/sun.awt=ALL-UNNAMED --add-exports=java.desktop/sun.java2d=ALL-UNNAMED -Dapple.laf.useScreenMenuBar=true -Xdock:name=TinyWorld -Xmx4096m -Xms4096m"
ICON_FILE="release/dmg/tinyworld.icns"
LICENSE_FILE="release/dmg/tmp/LICENSE"

# Use jpackage to generate a macOS app bundle
echo "Generating macOS app bundle (arch: $ARCH) ..."

jpackage \
  --type dmg \
  --dest release/artifacts \
  --input release/dmg/tmp \
  --main-jar "$MAIN_JAR" \
  --main-class "$MAIN_CLASS" \
  --name "TinyWorld" \
  --app-version "$RELEASE_VERSION" \
  --icon "$ICON_FILE" \
  --about-url "https://github.com/asaintsever/tinyworld" \
  --license-file "$LICENSE_FILE" \
  --copyright "Copyright 2021-2025 A. Saint-Sever" \
  --java-options "$JVM_OPTIONS"
