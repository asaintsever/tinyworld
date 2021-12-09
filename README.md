# TinyWorld

Travel the globe from your photos. You'll see how small the World is.

## Features

- 100% offline desktop app: no internet connection needed
- Available as portable app or container image 
- Catalog your photos: extract metadata, generate thumbnails
- Display thumbnails and info on virtual globe using extracted geolocation data
- Search & filter capabilities to easily navigate your photos (by dates, countries, ...)

## Requirements

- Java 11+ for portable app
- Docker or Podman for container image

## Deps

- NASA WorldWind Java: <https://github.com/NASAWorldWind/WorldWindJava>
- OpenSearch: <https://github.com/opensearch-project/OpenSearch>
- Metadata Extractor: <https://github.com/drewnoakes/metadata-extractor>

## Workarounds

### JOGL

Looks like there are some issues using JOGL library with Java 8+: see [here](https://forum.jogamp.org/InaccessibleObjectException-td4040284.html). A workaround is provided at <https://jogamp.org/bugzilla/show_bug.cgi?id=1317#c21>.

Edit `run-demo.bash` and insert `--add-exports` args:

```sh
java --add-exports java.base/java.lang=ALL-UNNAMED --add-exports java.desktop/sun.awt=ALL-UNNAMED --add-exports java.desktop/sun.java2d=ALL-UNNAMED -Xmx2048m ...
```

This is a temporary fix, monitor new releases of JOGL for proper resolution.
