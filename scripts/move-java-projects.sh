#!/bin/bash
set -e

REPO_ROOT=$(git rev-parse --show-toplevel 2> /dev/null)

# Move current spacewalk-java to temporary dir
git mv "$REPO_ROOT/java" "$REPO_ROOT/java-temp"
# Rebuild java and move spacewalk-java to correct position
mkdir "$REPO_ROOT/java"
git mv "$REPO_ROOT/java-temp" "$REPO_ROOT/java/spacewalk-java"
git commit -m "Move spacewalk-java code to subfolder"

# Move branding
git mv "$REPO_ROOT/branding" "$REPO_ROOT/java/branding"
git commit -m "Move branding to java dir"

# Move microservices
git mv "$REPO_ROOT/microservices/uyuni-java-parent" "$REPO_ROOT/java/uyuni-java-parent"
git commit -m "Move uyuni-java-parent to java directory"

git mv "$REPO_ROOT/microservices/uyuni-java-common" "$REPO_ROOT/java/uyuni-java-common"
git commit -m "Move uyuni-java-common to java directory"

git mv "$REPO_ROOT/microservices/coco-attestation" "$REPO_ROOT/java/coco-attestation"
git commit -m "Move coco-attestation to java directory"


