# TinyWorld

*Travel the globe from your photos. You'll see how small the World is.*

[![License](https://img.shields.io/github/license/asaintsever/tinyworld?style=for-the-badge)](https://github.com/asaintsever/tinyworld/blob/main/LICENSE)
[![GitHub All Releases](https://img.shields.io/github/downloads/asaintsever/tinyworld/total?style=for-the-badge)](https://github.com/asaintsever/tinyworld/releases)
[![Docker Pulls](https://img.shields.io/docker/pulls/asaintsever/tinyworld?style=for-the-badge)](https://hub.docker.com/r/asaintsever/tinyworld)
[![Latest release](https://img.shields.io/github/v/release/asaintsever/tinyworld?style=for-the-badge)](https://github.com/asaintsever/tinyworld/releases)

## Features

- Catalog your photos: metadata extraction & thumbnails generation
- Geolocalize photos on virtual globe with metadata in annotations
- Search & filter capabilities to easily navigate your photos (by dates, countries, ...)
- Offline mode (use local cache for globe data)
- Available as portable app, AppImage package and OCI image

## Supported Photo Formats

- JPEG/JPG
- PNG
- HEIF/HEIC

## Requirements

- Linux or WSL2 for AppImage
- Docker or Podman for OCI image

## Configuration

See [TinyWorld Configuration](cfg/README.md)

## Run

### Using Portable App

Untar/Unzip downloaded portable package (several flavors available: with Linux JRE, with Windows JRE) then run provided TinyWorld script:

```sh
# On Linux
<TinyWorld folder>/tinyworld.sh

# On Windows
<TinyWorld folder>\tinyworld.bat
```

By default, 4Gb of memory is set via Xmx/Xms Java options: edit the script to change any Java options.

### Using AppImage on Linux/WSL2

```sh
chmod +x TinyWorld-<release version>-x86_64.AppImage
./TinyWorld-<release version>-x86_64.AppImage
```

By default, 4Gb of memory is set via Xmx/Xms Java options. You can override this and add other options using the JAVA_OPTS env var:

```sh
# E.g. 2Gb max, 1Gb min
JAVA_OPTS="-Xmx2048m -Xms1024m" ./TinyWorld-<release version>-x86_64.AppImage
```

### Using OCI Image on Linux/WSL2

```sh
<docker|podman> run --rm -e DISPLAY -v "$HOME/.Xauthority:/root/.Xauthority:rw" -v "$HOME/.tinyworld:/root/.tinyworld" -v "$HOME/var/cache:/root/var/cache" --network host asaintsever/tinyworld:<release version>
```

By default, 4Gb of memory is set via Xmx/Xms Java options. You can override this and add other options using the JAVA_OPTS env var:

```sh
# E.g. 2Gb max, 1Gb min
<docker|podman> run --rm -e DISPLAY -e JAVA_OPTS="-Xmx2048m -Xms1024m" -v "$HOME/.Xauthority:/root/.Xauthority:rw" -v "$HOME/.tinyworld:/root/.tinyworld" -v "$HOME/var/cache:/root/var/cache" --network host asaintsever/tinyworld:<release version>
```

## Workarounds

### Scaling UI issue on High DPI resolutions

In case you experience scaling issue with High DPI (such as globe not filling the whole UI for example), you can disable UI scaling for TinyWorld application only by providing following option to the JVM:

```sh
-Dsun.java2d.uiScale=1.0
```

## Build

### Requirements

- Make
- Maven 3
- Java JDK 17
- zip/unzip *(to generate TinyWorld portable app for Windows)*
- Podman *(to generate TinyWorld OCI image)*
- FUSE *(to generate TinyWorld AppImage)*

    > *On Ubuntu 22.04+, install required FUSE library using `sudo apt install libfuse2`*

### Building from Source

You need to init your environment the first time (will install WorlWind jar and sources into your local Maven repo):

```sh
make init
```

Then you can generate all supported packages:

```sh
# Build TinyWorld portable app
make gen-portableapp

# Build TinyWorld AppImage
make gen-appimage

# Build TinyWorld OCI image
make gen-oci-image
```

## Releases

Instructions to release new versions of TinyWorld are available [here](RELEASE.md).

## Contributing

See [Contributing Guide](CONTRIBUTING.md)

## Deps & Credits

TinyWorld is built upon lots of great projects. Main ones are listed below.

- NASA WorldWind Java: <https://github.com/NASAWorldWind/WorldWindJava>
- OpenSearch: <https://github.com/opensearch-project/OpenSearch>
- Metadata Extractor: <https://github.com/drewnoakes/metadata-extractor>

Thumbnails for HEIF/HEIC photos are generated using [ImageMagick](https://imagemagick.org/).
