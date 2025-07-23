# Project: TinyWorld

The paths below are relative to the `tinyworld` root directory.

## Overview

This project is a desktop Java application allowing users to navigate their photo collections.

Features to implement:

- Catalog your photos: metadata extraction & thumbnails generation
- Geolocalize photos on virtual globe with metadata in annotations
- Search & filter capabilities to easily navigate your photos (by dates, countries, ...)

## General Instructions

- When generating new Java code, please follow the existing coding style.
- Ensure all new functions and classes have Javadoc comments.
- Add relevant unit tests for any meaningful new feature or function with assertions and comments explaining expected behaviour.
- All logs must use the SLF4J logging framework.
- All instructions to build, test, package and publish the project must be defined as a Makefile target in [GNUmakefile](./GNUmakefile) file.
- Project uses Lombok library to easily add getters/setters to Java classes.
- All code should be compatible with Java 17.

## Build system and Testing

- The project uses Maven 3+ to handle dependencies, plugins and build lifecycle.
- Tests leverage Java JUnit 5 test framework.
- To run unit tests, use command `gmake test`.

## Coding Style

- Coding and formatting config is described in file `./build-tools/src/main/resources/eclipse/my-eclipse-formatter-config.xml`.
- Always check new code or changes are well formatted using `gmake format` command.

## Project Architecture

TinyWorld is built on top of many open source frameworks and libraries. The main ones are:

- [NASA WorldWind Java](https://github.com/NASAWorldWind/WorldWindJava), to display photo location on a 3D globe and for TinyWorld UI
- [OpenSearch](https://github.com/opensearch-project/OpenSearch), to index and search photo metadata
- [Metadata Extractor](https://github.com/drewnoakes/metadata-extractor), to extract metadata (EXIF, IPTC, XMP ...) from photo files

### Components

#### Config - `./cfg` folder

This component allows to load and save TinyWorld's configuration from/to `tinyworld.yml` YAML file.

Dependencies: see [pom.xml](./cfg/pom.xml)

#### Metadata Extractor - `./metadata-extractor` folder

This component is in charge of extracting metadata from photo files using following code:

- [Extract.java](./metadata-extractor/src/main/java/asaintsever/tinyworld/metadata/extractor/Extract.java), to get list of photo files from a root directory and process them (delegating metadata extraction to [IPhotoProcess](./metadata-extractor/src/main/java/asaintsever/tinyworld/metadata/extractor/IPhotoProcess.java) interface).
- [PhotoObject.java](./metadata-extractor/src/main/java/asaintsever/tinyworld/metadata/extractor/PhotoObject.java), the main entity with methods to extract photo metadata and thumbnail and retrieve them.

Dependencies: see [pom.xml](./metadata-extractor/pom.xml)

#### Indexor - `./indexor` folder

Indexor depends on both Config and Metadata Extractor components. The main entity is [Indexor](./indexor/src/main/java/asaintsever/tinyworld/indexor/Indexor.java). It either starts or uses an existing OpenSearch cluster, load search templates into the cluster and exposes methods to manage the photo index (create, exists, delete) and photo metadata (add, get, search).

A test CLI is provided as an example: [IndexorCmd](./indexor/src/main/java/asaintsever/tinyworld/indexor/IndexorCmd.java).

Dependencies: see [pom.xml](./indexor/pom.xml)

#### UI - `./ui` folder

TinyWorld UI directly depends on Config and Indexor components. This is the component hosting the main application entry point, initializing the Indexor from the configuration and displaying the UI. 

It relies on both standard Javax API for basic UI components and NASA WorldWind UI widgets (renderers, layers, events) for close integration with NASA WorldWind Globe as it is the application runtime environment.

Dependencies: see [pom.xml](./ui/pom.xml)

## Regarding Dependencies

- Avoid introducing new external dependencies unless absolutely necessary.
- If a new dependency is required, please state the reason and pick a version that is compatible with TinyWorld technical stack and does not force updates of existing dependencies. If you cannot comply with this constraint, explain and ask for user approval before proceeding.
