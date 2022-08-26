#!/bin/bash

set -e

# clean
rm -rf release/tmp/3rd || true

mkdir -p release/tmp/3rd/linux
mkdir -p release/tmp/3rd/windows

# Generate 3rd-party license
touch "release/tmp/3rd/LICENSE-3RD-PARTY.txt"
echo -e "\n\n=====\nLicense for ImageMagick <https://imagemagick.org/>\n=====\n\n" >> "release/tmp/3rd/LICENSE-3RD-PARTY.txt"
curl -s -L https://raw.githubusercontent.com/asaintsever/tinyworld-utils/main/ImageMagick-LICENSE >> "release/tmp/3rd/LICENSE-3RD-PARTY.txt"

# Get 3rd-party software
curl -s -L --output release/tmp/3rd/linux/magick https://github.com/asaintsever/tinyworld-utils/releases/download/imagemagick-heic/magick
chmod +x release/tmp/3rd/linux/magick

curl -s -L --output release/tmp/3rd/windows/magick.exe https://github.com/asaintsever/tinyworld-utils/releases/download/imagemagick-heic/magick.exe
