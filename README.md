# TinyWorld

*Travel the globe from your photos. You'll see how small the World is.*

## Features

- Catalog your photos: metadata extraction & thumbnails generation
- Geolocalize photos on virtual globe with metadata in annotations
- Search & filter capabilities to easily navigate your photos (by dates, countries, ...)
- Offline mode (use local cache for globe data)
- Available as portable app, AppImage package and container image

## Supported Photo Formats

- JPEG/JPG
- PNG

## Requirements

- Java 11+ for portable app
- Docker or Podman for container image

## Configuration

See [TinyWorld Configuration](cfg/README.md)

## Run

### Using AppImage on Linux/WSL2

```sh
chmod +x TinyWorld-<release version>-x86_64.AppImage
./TinyWorld-<release version>-x86_64.AppImage
```

### Using Container Image on Linux/WSL2

```sh
<docker|podman> run --rm -e DISPLAY -v "$HOME/.Xauthority:/root/.Xauthority:rw" --network host <TinyWorld image> 
```

## Building from Source

```sh
# Build TinyWorld portable app
make gen-portableapp

# Build TinyWorld AppImage
make gen-appimage

# Build TinyWorld container image
make gen-container-image
```

## Deps & Credits

TinyWorld is built upon lots of great open source projects. Main ones are listed below.

- NASA WorldWind Java: <https://github.com/NASAWorldWind/WorldWindJava>
- OpenSearch: <https://github.com/opensearch-project/OpenSearch>
- Metadata Extractor: <https://github.com/drewnoakes/metadata-extractor>
