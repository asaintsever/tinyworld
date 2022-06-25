#!/bin/bash

set -e

RELEASE_VERSION=$1

rm -rf release/tmp || true
rm -rf release/portable/tinyworld-* || true
mkdir -p release/portable/tinyworld-linux-jre
mkdir -p release/portable/tinyworld-windows-jre
mkdir -p release/portable/tinyworld-nojre

cp -R release/portable/tmp/* release/portable/tinyworld-linux-jre
cp -R release/portable/tmp/* release/portable/tinyworld-windows-jre
cp -R release/portable/tmp/* release/portable/tinyworld-nojre

cp -R release/portable/linux-jre/tinyworld.* release/portable/tinyworld-linux-jre
cp -R release/portable/windows-jre/tinyworld.* release/portable/tinyworld-windows-jre
cp -R release/portable/nojre/tinyworld.* release/portable/tinyworld-nojre

chmod +x release/portable/tinyworld-linux-jre/tinyworld.sh
chmod +x release/portable/tinyworld-nojre/tinyworld.sh

# No JRE
echo "Portable No JRE ..."
tar -C release/portable -czf release/artifacts/TinyWorld-${RELEASE_VERSION}-x86_64.tgz tinyworld-nojre

# Linux - Add JRE 17
echo "Portable Linux JRE ..."
curl -s -L --create-dirs --output release/tmp/jre_linux.tar.gz https://github.com/asaintsever/tinyworld-utils/releases/download/jre-distro/OpenJDK17U-jre_x64_linux_hotspot_17.0.3_7.tar.gz
tar -xzf release/tmp/jre_linux.tar.gz -C release/portable/tinyworld-linux-jre
mv release/portable/tinyworld-linux-jre/jdk-* release/portable/tinyworld-linux-jre/jre
tar -C release/portable -czf release/artifacts/TinyWorld-Linux-JRE-${RELEASE_VERSION}-x86_64.tgz tinyworld-linux-jre

# Windows - Add JRE 17
echo "Portable Windows JRE ..."
curl -s -L --create-dirs --output release/tmp/jre_win.zip https://github.com/asaintsever/tinyworld-utils/releases/download/jre-distro/OpenJDK17U-jre_x64_windows_hotspot_17.0.3_7.zip
unzip -q release/tmp/jre_win.zip -d release/portable/tinyworld-windows-jre
mv release/portable/tinyworld-windows-jre/jdk-* release/portable/tinyworld-windows-jre/jre
cd release/portable && zip -q -r ../artifacts/TinyWorld-Windows-JRE-${RELEASE_VERSION}-x86_64.zip tinyworld-windows-jre
