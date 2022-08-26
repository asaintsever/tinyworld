#!/bin/bash

set -e

IMAGE_FQIN=$1
RELEASE_VERSION=$2

# Include 3rd party software
cp release/tmp/3rd/LICENSE-3RD-PARTY.txt release/oci-image/tmp
cp release/tmp/3rd/linux/* release/oci-image/tmp

# Build OCI image
podman build -t ${IMAGE_FQIN}:${RELEASE_VERSION} release/oci-image
