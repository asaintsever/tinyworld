#!/bin/bash

set -e

ARCH=$1
RELEASE_VERSION=$2

rm -rf release/appimage/AppDir || true
mkdir -p release/appimage/AppDir/usr/bin

cp -R release/appimage/tmp/* release/appimage/AppDir/usr/bin

cp release/appimage/tinyworld.sh release/appimage/AppDir/usr/bin
cp release/appimage/tinyworld.desktop release/appimage/AppDir
cp release/appimage/tinyworldicon.png release/appimage/AppDir

cp release/appimage/bin/AppRun-${ARCH} release/appimage/AppDir/AppRun

chmod +x release/appimage/AppDir/AppRun
chmod +x release/appimage/AppDir/usr/bin/tinyworld.sh

# Include 3rd party software
cp release/tmp/3rd/LICENSE-3RD-PARTY.txt release/appimage/AppDir/usr/bin
for thirdparty in release/tmp/3rd/*-linux-${ARCH}
do
  cp ${thirdparty} "release/appimage/AppDir/usr/bin/$(basename "${thirdparty%-linux-${ARCH}}")"
done

# Add JRE 17
curl -s -L --create-dirs --output release/tmp/jre-linux-${ARCH}.tar.gz https://github.com/asaintsever/tinyworld-utils/releases/download/jre-distro/OpenJDK17U-jre_${ARCH}_linux_hotspot.tar.gz
tar -xzf release/tmp/jre-linux-${ARCH}.tar.gz -C release/appimage/AppDir/usr/bin
mv release/appimage/AppDir/usr/bin/jdk-* release/appimage/AppDir/usr/bin/jre

# Build AppImage
release/appimage/bin/appimagetool-${ARCH} release/appimage/AppDir release/artifacts/tinyworld-${ARCH}-${RELEASE_VERSION}.AppImage