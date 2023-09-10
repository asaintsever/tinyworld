#!/bin/bash

set -e

echo "Get 3rd-party software ..."

# clean
rm -rf release/tmp/3rd || true

mkdir -p release/tmp/3rd

# Generate 3rd-party license
touch "release/tmp/3rd/LICENSE-3RD-PARTY.txt"
echo -e "\n\n=====\nLicense for ImageMagick <https://imagemagick.org/>\n=====\n\n" >> "release/tmp/3rd/LICENSE-3RD-PARTY.txt"
curl -s -L https://raw.githubusercontent.com/asaintsever/tinyworld-utils/main/ImageMagick-LICENSE >> "release/tmp/3rd/LICENSE-3RD-PARTY.txt"

# Get 3rd-party software
curl -s -L --output release/tmp/3rd/magick-linux-x86_64 https://github.com/asaintsever/tinyworld-utils/releases/download/imagemagick-heic/magick-linux-x86_64
curl -s -L --output release/tmp/3rd/magick-linux-aarch64 https://github.com/asaintsever/tinyworld-utils/releases/download/imagemagick-heic/magick-linux-aarch64
chmod +x release/tmp/3rd/magick-linux-*

curl -s -L --output release/tmp/3rd/magick-windows-x86_64.exe https://github.com/asaintsever/tinyworld-utils/releases/download/imagemagick-heic/magick-windows-x86_64.exe
