SHELL=/bin/bash

RELEASE_VERSION:=$(shell cat VERSION)

OWNER:=asaintsever
REPO:=tinyworld
APPIMAGE_NAME:=TinyWorld
IMAGE_FQIN:=asaintsever/tinyworld

.SILENT: ;  	# No need for @
.ONESHELL: ; 	# Single shell for a target (required to properly use local variables)
.PHONY: help clean test package run-ui run-indexor pre-release gen-portableapp gen-container-image gen-appimage release
.DEFAULT_GOAL := help

help: ## Show Help
	grep -E '^[a-zA-Z_-]+:.*?## .*$$' Makefile | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

clean: ## Clean
	mvn clean
	rm -rf release/artifacts || true

test: clean ## Run tests
	mvn test

package: clean ## Package
	mvn package -Dmaven.test.skip=true

run-indexor: ## Run Indexor test program
	read -p "Full path to root directory to index: " pathToIndex
	mvn package -Dmaven.test.skip=true -Dindexor.cmd.path=$$pathToIndex -Dindexor.cmd.clearIndex=true -Dindexor.cmd.allowUpdate=false -P indexorCmd

run-ui: ## Run TinyWorld UI
	mvn package -Dmaven.test.skip=true -P UI

pre-release:
	mkdir -p release/artifacts
	chmod +x release/release-helper.sh
	chmod +x release/appimage/appdir-gen.sh
	chmod +x release/appimage/x86_64/appimagetool-x86_64

gen-portableapp: pre-release ## Generate TinyWorld Portable App
	echo "Build Portable App ..."
	echo "TODO"

# https://docs.appimage.org/packaging-guide/manual.html
# https://github.com/AppImage/AppImageKit/wiki/Bundling-Java-apps#option-2-bundling-jre-manually
gen-appimage: package pre-release ## Generate TinyWorld AppImage
	set -e
	echo "Build AppImage package ..."
	release/release-helper.sh release/appimage
	release/appimage/appdir-gen.sh
	release/appimage/x86_64/appimagetool-x86_64 release/appimage/AppDir release/artifacts/TinyWorld-${RELEASE_VERSION}-x86_64.AppImage

gen-container-image: package pre-release ## Generate TinyWorld Container Image
	set -e
	echo "Build container image ..."
	release/release-helper.sh release/docker
	podman build -t ${IMAGE_FQIN}:${RELEASE_VERSION} release/docker

release: test gen-container-image gen-appimage gen-portableapp ## Release
	read -p "Publish image (y/n)? " answer
	case $$answer in \
	y|Y ) \
		podman login; \
		podman push ${IMAGE_FQIN}:${RELEASE_VERSION}; \
		if [ "$$?" -ne 0 ]; then \
			echo "Unable to publish image"; \
			exit 1; \
		fi; \
	;; \
	* ) \
		echo "Image not published"; \
	;; \
	esac
	echo "Releasing artifacts ..."
	read -p "- Github user name to use for release: " username
	echo "- Creating release"
	id=$$(curl -u $$username -s -X POST "https://api.github.com/repos/${OWNER}/${REPO}/releases" -d '{"tag_name": "v'${RELEASE_VERSION}'", "name": "v'${RELEASE_VERSION}'", "draft": true, "body": ""}' | jq '.id')
	if [ "$$?" -ne 0 ]; then \
		echo "Unable to create release"; \
		echo $$id; \
		exit 1; \
	fi
	echo "- Release id=$$id"
	echo
	echo "- Publishing release artifacts"
	for asset_file in $(shell ls ./release/artifacts); do \
		asset_absolute_path=$$(realpath $$asset_file); \
		echo "Adding file $$asset_absolute_path"; \
		echo; \
		asset_filename=$$(basename $$asset_absolute_path); \
		curl -u $$username -s --data-binary @"$$asset_absolute_path" -H "Content-Type: application/octet-stream" "https://uploads.github.com/repos/${OWNER}/${REPO}/releases/$$id/assets?name=$$asset_filename"; \
		if [ "$$?" -ne 0 ]; then \
			echo "Unable to publish artifact $$asset_absolute_path"; \
			exit 1; \
		fi; \
		echo; \
	done
	echo
	echo
	read -p "- Confirm release ok at https://api.github.com/repos/${OWNER}/${REPO}/releases/$$id (y/[n])? " answer
	case $$answer in \
	y|Y ) \
		curl -u $$username -s -X PATCH "https://api.github.com/repos/${OWNER}/${REPO}/releases/$$id" -d '{"draft": false}'; \
		if [ "$$?" -ne 0 ]; then \
			echo "Unable to finish release"; \
			exit 1; \
		fi; \
	;; \
	* ) \
		curl -u $$username -s -X DELETE "https://api.github.com/repos/${OWNER}/${REPO}/releases/$$id"; \
		echo "Aborted"; \
	;; \
	esac
