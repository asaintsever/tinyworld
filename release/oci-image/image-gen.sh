#!/bin/bash

set -e

IMAGE_FQIN=$1
RELEASE_VERSION=$2

# Build using either podman or docker
container_runtime=$(basename $(command -v podman)  2> /dev/null || echo docker)

# Include 3rd party software
cp release/tmp/3rd/LICENSE-3RD-PARTY.txt release/oci-image/tmp
cp release/tmp/3rd/*-linux-* release/oci-image/tmp

# Build OCI image
$container_runtime build -t ${IMAGE_FQIN}:${RELEASE_VERSION} release/oci-image
