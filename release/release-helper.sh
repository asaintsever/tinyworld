#!/bin/bash

set -e

# clean
rm -rf "$1/tmp" || true

mkdir -p "$1/tmp/config"
mkdir -p "$1/tmp/deps"

# copy config, deps & jars, license
cp ui/target/config/*.xml "$1/tmp/config"
cp ui/target/deps/* "$1/tmp/deps"
cp ui/target/*.jar "$1/tmp"
cp LICENSE "$1/tmp"
