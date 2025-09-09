SHELL=/bin/bash

RELEASE_VERSION:=$(shell cat VERSION)

CONTAINER_RUNTIME:=$(shell basename $$(command -v podman)  2> /dev/null || echo docker)

OWNER:=asaintsever
REPO:=tinyworld
APPIMAGE_NAME:=TinyWorld
IMAGE_FQIN:=asaintsever/tinyworld

.SILENT: ;  	# No need for @
.ONESHELL: ; 	# Single shell for a target (required to properly use local variables)
.PHONY: help init clean format test package run-ui run-ui-gl-sw run-indexor pre-release gen-portableapp gen-oci-image gen-appimage gen-dmg next-version release-github
.DEFAULT_GOAL := help

help: ## Show Help
	grep -E '^[a-zA-Z_-]+:.*?## .*$$' *makefile | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

init: ## Init build (to run once)
	set -e
	chmod +x release/*.sh
	chmod +x release/appimage/appdir-gen.sh
	chmod +x release/appimage/bin/appimagetool-*
	chmod +x release/portable/portableapp-gen.sh
	chmod +x release/oci-image/image-gen.sh
	chmod +x release/dmg/dmg-gen.sh
	mvn validate

clean: ## Clean
	mvn clean
	rm -rf release/artifacts || true
	mkdir -p release/artifacts

format: ## Format code
	mvn install -pl build-tools
	mvn net.revelc.code.formatter:formatter-maven-plugin:format

test: clean ## Run tests
ifeq ($(TEST_MODULE),)
	mvn test
else
	mvn test -pl $(TEST_MODULE) -am
endif

package: clean ## Package
	mvn package -Dmaven.test.skip=true

run-indexor: ## Run Indexor test program
	read -p "Full path to root directory to index: " pathToIndex
	read -p "Clear previously indexed data (true/false): " clearIndex
	read -p "Allow updates of existing photo metadata in index (true/false): " allowUpdate
	mvn package -Dmaven.test.skip=true -Dindexor.cmd.path=$$pathToIndex -Dindexor.cmd.clearIndex=$$clearIndex -Dindexor.cmd.allowUpdate=$$allowUpdate -P indexorCmd

run-ui: ## Run TinyWorld UI
	mvn package -Dmaven.test.skip=true -P UI

run-ui-gl-sw: ## Run TinyWorld UI with OpenGL software rendering
	LIBGL_ALWAYS_SOFTWARE=1 mvn package -Dmaven.test.skip=true -P UI 

pre-release:
	release/get-3rd-party.sh

gen-portableapp: package pre-release ## Generate TinyWorld Portable App (Linux - x86_64 aarch64, Windows - x86_64)
	set -e
	echo "Build Portable App ..."
	release/release-helper.sh release/portable
	release/portable/portableapp-gen.sh ${RELEASE_VERSION}

# https://docs.appimage.org/packaging-guide/manual.html
# https://github.com/AppImage/AppImageKit/wiki/Bundling-Java-apps#option-2-bundling-jre-manually
gen-appimage: package pre-release ## Generate TinyWorld AppImage (Linux - For current architecture)
	set -e
	arch=$$(uname -m)
	echo "Build AppImage package (arch=$$arch)..."
	release/release-helper.sh release/appimage
	release/appimage/appdir-gen.sh $$arch ${RELEASE_VERSION}

gen-oci-image: package pre-release ## Generate TinyWorld OCI Image
	set -e
	echo "Build OCI image ..."
	release/release-helper.sh release/oci-image
	release/oci-image/image-gen.sh ${IMAGE_FQIN} ${RELEASE_VERSION}

gen-dmg: package ## Generate TinyWorld DMG (macOS - For current architecture)
	set -e
	arch=$$(uname -m)
	echo "Build DMG package (arch=$$arch)..."
	release/release-helper.sh release/dmg
	release/dmg/dmg-gen.sh $$arch ${RELEASE_VERSION}

next-version: ## Set next version
	set -e
	read -p "Enter new TinyWorld version: " twNewVer
	mvn versions:set -DnewVersion=$$twNewVer
	echo -n $$twNewVer > VERSION

release-github: test gen-oci-image gen-appimage gen-portableapp ## Release on GitHub
	read -p "Publish image (y/n)? " answer
	case $$answer in \
	y|Y ) \
		$(CONTAINER_RUNTIME) login; \
		$(CONTAINER_RUNTIME) push ${IMAGE_FQIN}:${RELEASE_VERSION}; \
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
	for asset_file in $$(ls ./release/artifacts); do \
		asset_absolute_path=$$(realpath ./release/artifacts/$$asset_file); \
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
