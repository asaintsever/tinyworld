#!/bin/bash

set -e

rm -rf release/appimage/AppDir || true
mkdir -p release/appimage/AppDir/usr/bin

cp -R release/appimage/tmp/* release/appimage/AppDir/usr/bin

cp release/appimage/tinyworld.sh release/appimage/AppDir/usr/bin
cp release/appimage/tinyworld.desktop release/appimage/AppDir
cp release/appimage/tinyworldicon.png release/appimage/AppDir

cp release/appimage/x86_64/AppRun-x86_64 release/appimage/AppDir/AppRun

chmod +x release/appimage/AppDir/AppRun
chmod +x release/appimage/AppDir/usr/bin/tinyworld.sh

# Add JRE 17
curl -s -L --create-dirs --output release/tmp/jre_linux.tar.gz https://github.com/asaintsever/tinyworld-utils/releases/download/jre-distro/OpenJDK17U-jre_x64_linux_hotspot_17.0.3_7.tar.gz
tar -xzf release/tmp/jre_linux.tar.gz -C release/appimage/AppDir/usr/bin
mv release/appimage/AppDir/usr/bin/jdk-* release/appimage/AppDir/usr/bin/jre
