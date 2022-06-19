#!/bin/bash

set -e

rm -rf release/portable/tinyworld-* || true
mkdir -p release/portable/tinyworld-linux-jre
mkdir -p release/portable/tinyworld-windows-jre
mkdir -p release/portable/tinyworld-nojre

cp -R release/portable/tmp/* release/portable/tinyworld-linux-jre
cp -R release/portable/tmp/* release/portable/tinyworld-windows-jre
cp -R release/portable/tmp/* release/portable/tinyworld-nojre

cp release/portable/linux-jre/tinyworld.sh release/portable/tinyworld-linux-jre
cp release/portable/windows-jre/tinyworld.bat release/portable/tinyworld-windows-jre
cp -R release/portable/nojre/tinyworld.* release/portable/tinyworld-nojre

chmod +x release/portable/tinyworld-linux-jre/tinyworld.sh
chmod +x release/portable/tinyworld-nojre/tinyworld.sh

# Linux - Add JRE 17
tar -xvzf release/jre/linux/OpenJDK17U-jre_x64_linux_*.tar.gz -C release/portable/tinyworld-linux-jre
mv release/portable/tinyworld-linux-jre/jdk-* release/portable/tinyworld-linux-jre/jre
tar TODO

# Windows - Add JRE 17
unzip TODO
mv release/portable/tinyworld-windows-jre/jdk-* release/portable/tinyworld-windows-jre/jre
zip TODO
