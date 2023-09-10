#!/bin/bash

set -e

RELEASE_VERSION=$1

target_platforms=(linux windows)
windows_arch=(x86_64)
linux_arch=(x86_64 aarch64)

rm -rf release/portable/tinyworld-* || true

for platform in "${target_platforms[@]}"; do
  arch_array="${platform}_arch[@]"
  for arch in "${!arch_array}"; do
    echo "Generating TinyWorld portable release for ${platform} (arch=${arch}) ..."
    mkdir -p release/portable/tinyworld-jre-${platform}-${arch}/tools
    
    # Copy TinyWorld libraries
    cp -R release/portable/tmp/* release/portable/tinyworld-jre-${platform}-${arch}

    # Copy TinyWorld launcher
    cp -R release/portable/${platform}-jre/tinyworld.* release/portable/tinyworld-jre-${platform}-${arch}
    if [ "${platform}" == "linux" ]; then
      chmod +x release/portable/tinyworld-jre-${platform}-${arch}/tinyworld.sh || true
    fi

    # Include 3rd party software
    cp release/tmp/3rd/LICENSE-3RD-PARTY.txt release/portable/tinyworld-jre-${platform}-${arch}
    for thirdparty in release/tmp/3rd/*-${platform}-${arch}*
    do
      if [ "${platform}" == "linux" ]; then
        cp ${thirdparty} "release/portable/tinyworld-jre-${platform}-${arch}/tools/$(basename "${thirdparty%-${platform}-${arch}}")"
      else # Windows
        cp ${thirdparty} "release/portable/tinyworld-jre-${platform}-${arch}/tools/$(basename "${thirdparty%-${platform}-${arch}.exe}").exe"
      fi
    done
    
    # Add JRE 17
    if [ "${platform}" == "linux" ]; then
      curl -s -L --create-dirs --output release/tmp/jre-${platform}-${arch}.tar.gz https://github.com/asaintsever/tinyworld-utils/releases/download/jre-distro/OpenJDK17U-jre_${arch}_${platform}_hotspot.tar.gz
      tar -xzf release/tmp/jre-${platform}-${arch}.tar.gz -C release/portable/tinyworld-jre-${platform}-${arch}
      mv release/portable/tinyworld-jre-${platform}-${arch}/jdk-* release/portable/tinyworld-jre-${platform}-${arch}/jre
      tar -C release/portable -czf release/artifacts/tinyworld-jre-${platform}-${arch}-${RELEASE_VERSION}.tgz tinyworld-jre-${platform}-${arch}
    else # Windows
      curl -s -L --create-dirs --output release/tmp/jre-${platform}-${arch}.zip https://github.com/asaintsever/tinyworld-utils/releases/download/jre-distro/OpenJDK17U-jre_${arch}_${platform}_hotspot.zip
      unzip -q release/tmp/jre-${platform}-${arch}.zip -d release/portable/tinyworld-jre-${platform}-${arch}
      mv release/portable/tinyworld-jre-${platform}-${arch}/jdk-* release/portable/tinyworld-jre-${platform}-${arch}/jre
      cd release/portable && zip -q -r ../artifacts/tinyworld-jre-${platform}-${arch}-${RELEASE_VERSION}.zip tinyworld-jre-${platform}-${arch}
    fi
    echo "... Done"
  done
done
