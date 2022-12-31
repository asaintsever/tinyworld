# Workarounds

## Scaling UI issue on High DPI resolutions

In case you experience scaling issue with High DPI (such as globe not filling the whole UI for example), you can disable UI scaling for TinyWorld application only by providing following option to the JVM:

```sh
-Dsun.java2d.uiScale=1.0
```
