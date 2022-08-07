# Contributing to TinyWorld

## Code Formatting

Before pushing your contribution, run the dedicated `format` target to ensure formatting consistency across all code base:

```sh
make format
```

## Logging

- The logging framework to use is **SLF4J** (with LOG4J as backend). *Do not introduce another framework*. You may find references to JUL because of some dependencies making use of it (WorldWind for e.g.): such calls are redirected to SLF4J via the "JUL to SLF4J bridge".

## Testing

- For each new entity or feature, add meaningful tests using **JUnit 5** framework.
