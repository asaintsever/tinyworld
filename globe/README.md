# TinyWorld Globe

TinyWorld Globe leverages [NASA WorldWind Java](https://github.com/NASAWorldWind/WorldWindJava) as Globe & 2D Map provider.

## WWJ Utilities

NASA WorlWind comes with lot of examples and utilities. Exhaustive list can be found at <https://worldwind.arc.nasa.gov/java/examples/#anchor>.

Below is a list of the most useful ones for TinyWorld usage:

- Display Globe/2D Map & layers: `deps/wwj/wwj-utils.sh`
- Globe only: `deps/wwj/wwj-utils.sh gov.nasa.worldwindx.examples.SimplestPossibleExample`
- Annotations:
  - `deps/wwj/wwj-utils.sh gov.nasa.worldwindx.examples.Annotations`
  - `deps/wwj/wwj-utils.sh gov.nasa.worldwindx.examples.AnnotationControls`
- Manage local cache: `deps/wwj/wwj-utils.sh gov.nasa.worldwindx.examples.util.cachecleaner.DataCacheViewer`
- Bulk download layers: `deps/wwj/wwj-utils.sh gov.nasa.worldwindx.examples.BulkDownload`

## Workarounds

### JOGL

Looks like there are some issues using JOGL library with Java 8+: see [here](https://forum.jogamp.org/InaccessibleObjectException-td4040284.html). A workaround is provided at <https://jogamp.org/bugzilla/show_bug.cgi?id=1317#c21>.

Add `--add-exports` args to java command line:

```sh
java --add-exports java.base/java.lang=ALL-UNNAMED --add-exports java.desktop/sun.awt=ALL-UNNAMED --add-exports java.desktop/sun.java2d=ALL-UNNAMED ...
```

This fix has already been applied to [deps/wwj/wwj-utils.sh](deps/wwj/wwj-utils.sh) script.
