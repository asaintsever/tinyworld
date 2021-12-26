# TinyWorld

*Travel the globe from your photos. You'll see how small the World is.*

## Features

- Catalog your photos: metadata extraction & thumbnails generation
- Geolocalize photos on virtual globe with metadata in annotations
- Search & filter capabilities to easily navigate your photos (by dates, countries, ...)
- Offline mode (use local cache for globe data)
- Available as portable app or container image

## Requirements

- Java 11+ for portable app
- Docker or Podman for container image

## Deps

- NASA WorldWind Java: <https://github.com/NASAWorldWind/WorldWindJava>
- OpenSearch: <https://github.com/opensearch-project/OpenSearch>
- Metadata Extractor: <https://github.com/drewnoakes/metadata-extractor>

## WWJ Utilities

Nasa WorlWind comes with lot of examples and utilities. Exhaustive list can be found at <https://worldwind.arc.nasa.gov/java/examples/#anchor>.

Below is a list of the most useful ones for TinyWorld usage:

- Display globe/flat map/layers, online/offline mode: `globe/deps/wwj/wwj-utils.sh`
- Globe only: `globe/deps/wwj/wwj-utils.sh gov.nasa.worldwindx.examples.SimplestPossibleExample`
- Annotations:
  - `globe/deps/wwj/wwj-utils.sh gov.nasa.worldwindx.examples.Annotations`
  - `globe/deps/wwj/wwj-utils.sh gov.nasa.worldwindx.examples.AnnotationControls`
- Manage local cache: `globe/deps/wwj/wwj-utils.sh gov.nasa.worldwindx.examples.util.cachecleaner.DataCacheViewer`
- Bulk download layers: `globe/deps/wwj/wwj-utils.sh gov.nasa.worldwindx.examples.BulkDownload`

## Workarounds

### JOGL

Looks like there are some issues using JOGL library with Java 8+: see [here](https://forum.jogamp.org/InaccessibleObjectException-td4040284.html). A workaround is provided at <https://jogamp.org/bugzilla/show_bug.cgi?id=1317#c21>.

Add `--add-exports` args to java command line:

```sh
java --add-exports java.base/java.lang=ALL-UNNAMED --add-exports java.desktop/sun.awt=ALL-UNNAMED --add-exports java.desktop/sun.java2d=ALL-UNNAMED ...
```

This fix has already been applied to [globe/deps/wwj/wwj-utils.sh](globe/deps/wwj/wwj-utils.sh) script.
