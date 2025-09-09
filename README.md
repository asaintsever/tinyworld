# TinyWorld <img src="./tinyworldicon.jpg" width="24" height="24">

*Travel the globe from your photos. You'll see how small the World is.*

[![License](https://img.shields.io/github/license/asaintsever/tinyworld?style=for-the-badge)](https://github.com/asaintsever/tinyworld/blob/main/LICENSE)
[![GitHub All Releases](https://img.shields.io/github/downloads/asaintsever/tinyworld/total?style=for-the-badge)](https://github.com/asaintsever/tinyworld/releases)
[![Docker Pulls](https://img.shields.io/docker/pulls/asaintsever/tinyworld?style=for-the-badge)](https://hub.docker.com/r/asaintsever/tinyworld)
[![Latest release](https://img.shields.io/github/v/release/asaintsever/tinyworld?style=for-the-badge)](https://github.com/asaintsever/tinyworld/releases)

![](https://github.com/asaintsever/tinyworld/releases/download/demo/tinyworld.gif)

> [!IMPORTANT]
>
> This project is still WIP.

## Features

- Catalog your photos: metadata extraction & thumbnails generation
- Geolocalize photos on virtual globe with metadata in annotations
- Search & filter capabilities to easily navigate your photos (by dates, countries, ...)
- Offline mode (use local cache for globe data)
- Available as portable app, AppImage package, DMG package and OCI image *(i.e. container)*

## Supported Photo Formats

- JPEG/JPG
- PNG
- HEIF/HEIC

## Requirements

- Windows *(x64)* or Linux *(x64 or aarch64)* for Portable App
- Linux *(x64 or aarch64)* for AppImage
- macOS *(aarch64)* for DMG package
- Docker or [Podman](https://podman.io/) for OCI image

## Configuration

See [TinyWorld Configuration](cfg/README.md)

## Run

### Using Portable App

Untar/Unzip downloaded portable package (several flavors available: with Linux JRE x64 or aarch64, with Windows JRE x64) then run provided TinyWorld script:

```sh
# On Linux (x64 or aarch64)
<TinyWorld folder>/tinyworld.sh

# On Windows (x64)
<TinyWorld folder>\tinyworld.bat
```

By default, 4Gb of memory is set via Xmx/Xms Java options: edit the script to change any Java options.

> [!IMPORTANT]
> The `tools` subfolder **must be added** to your PATH environment variable.

### Using AppImage on Linux

```sh
chmod +x tinyworld-<x86_64|aarch64>-<release version>.AppImage

./tinyworld-<x86_64|aarch64>-<release version>.AppImage
```

By default, 4Gb of memory is set via Xmx/Xms Java options. You can override this and add other options using the JAVA_OPTS env var:

```sh
# E.g. 2Gb max, 1Gb min
JAVA_OPTS="-Xmx2048m -Xms1024m" ./tinyworld-<x86_64|aarch64>-<release version>.AppImage
```

### Using DMG package on macOS

Download the DMG package and double-clic on the file. Drag and drop the application icon to the Applications folder to install the application. A TinyWorld app should now be available in the Launchpad.

### Using OCI Image

```sh
<docker|podman> run --rm -e DISPLAY -v "$HOME/.Xauthority:/root/.Xauthority:rw" -v "$HOME/.tinyworld:/root/.tinyworld" -v "$HOME/var/cache:/root/var/cache" --network host asaintsever/tinyworld:<release version>
```

By default, 4Gb of memory is set via Xmx/Xms Java options. You can override this and add other options using the JAVA_OPTS env var:

```sh
# E.g. 2Gb max, 1Gb min
<docker|podman> run --rm -e DISPLAY -e JAVA_OPTS="-Xmx2048m -Xms1024m" -v "$HOME/.Xauthority:/root/.Xauthority:rw" -v "$HOME/.tinyworld:/root/.tinyworld" -v "$HOME/var/cache:/root/var/cache" --network host asaintsever/tinyworld:<release version>
```

### From Source

> *Requires GNU Make 4+, Java JDK 17+, Maven 3+*

See [instructions](ui/TEST.md).

## Build

### Requirements

- GNU Make 4+

    > *On macOS, you can install/update GNU Make using [brew](https://brew.sh/): `brew install make`*

- [jq](https://jqlang.github.io/jq/) *(used to parse responses from GitHub API)*
- Maven 3+
- Java JDK 17+
- zip/unzip *(to generate TinyWorld portable app for Windows)*
- Docker or Podman *(to generate TinyWorld OCI image)*
- [FUSE](https://github.com/libfuse/libfuse) *(to generate TinyWorld AppImage)*

    > *On Ubuntu / Debian, install required FUSE library using `sudo apt install libfuse2`*
    >
    > *On Chrome OS, install required FUSE library using `sudo apt install fuse`*

> [!NOTE]
> *For AppImage package, you'll only be able to generate the package for the platform you're running on (e.g. aarch64 AppImage if you use an arm/aarch64 platform).*

### Building from Source

You need to init your environment the first time (will install WorlWind jar and sources into your local Maven repo):

```sh
make init
```

Then you can generate all supported packages:

```sh
# Build portable app
make gen-portableapp

# Build AppImage
make gen-appimage

# Build OCI image
make gen-oci-image

# Build macOS DMG package
make gen-dmg
```

## Releases

Instructions to release new versions of TinyWorld are available [here](RELEASE.md).

## Contributing

See [Contributing Guide](CONTRIBUTING.md)

## Deps & Credits

TinyWorld is built upon lots of great projects. Main ones are:

- NASA WorldWind Java: <https://github.com/NASAWorldWind/WorldWindJava>
- OpenSearch: <https://github.com/opensearch-project/OpenSearch>
- Metadata Extractor: <https://github.com/drewnoakes/metadata-extractor>

Thumbnails for HEIF/HEIC photos are generated using [ImageMagick](https://imagemagick.org/).
