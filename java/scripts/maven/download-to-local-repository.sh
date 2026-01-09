#!/bin/bash
#
# Copyright (c) 2025 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.

set -e

function usage() {
    echo "$0 [-f|--force] [-h|--help] <config-file> <target-directory> <version-group>"
    echo
    echo "Where:"
    echo "    config-file       The path of the obs-to-maven configuration"
    echo "    target-directory  The directory where the repository and the cache will be created"
    echo "    group-directory   The directory used to group all the dependencies for a specific version"
    echo
    echo "Parameters:"
    echo "    -f, --force       Forces the execution of obs-to-maven"
    echo "    -h, --help        Prints this help screen"
}

FORCE_OBS_TO_MAVEN=false

while [[ $# -gt 0 ]]
do
    case $1 in
        -f|--force)
            FORCE_OBS_TO_MAVEN=true
            shift
            ;;

        -h|--help)
            usage
            exit 0
            ;;

        *)
            if [[ -z "$CONFIG_FILE" ]]; then
                CONFIG_FILE="$1"
            elif [[ -z "$TARGET_DIR" ]]; then
                TARGET_DIR="$1"
            elif [[ -z "$VERSION_GROUP" ]]; then
                VERSION_GROUP="$1"
            else
                echo "Error: Too many arguments provided: $1" >&2
                usage
                exit 1
            fi

            shift
            ;;
    esac
done

if [ -z "$TARGET_DIR" ] || [ -z "$VERSION_GROUP" ]; then
    echo "Error: Missing mandatory parameter" >&2
    usage
    exit 1
fi

if [ ! -d "$TARGET_DIR" ]; then
    echo "Error: Target directory does not exist" >&2
    exit 2
fi

REPOSITORY_DIR=$TARGET_DIR/repository/$VERSION_GROUP
CACHE_DIR=$TARGET_DIR/.obs-to-maven-cache
EXECUTION_FILE=$REPOSITORY_DIR/last-execution

if [ ! -f "$CONFIG_FILE" ]; then
    echo "Cannot find obs-to-maven configuration file $CONFIG_FILE. Skipping."
    exit 0
fi

if [ ! -d "$REPOSITORY_DIR" ]; then
    mkdir -p "$REPOSITORY_DIR"
fi

# Evaluate if obs-to-maven needs to run
LAST_RUN_TIME=$(stat -c %Y "$EXECUTION_FILE" 2>/dev/null || echo 0)
CONFIG_MODIFICATION_TIME=$(stat -c %Y "$CONFIG_FILE")

if (( CONFIG_MODIFICATION_TIME > LAST_RUN_TIME )) || $FORCE_OBS_TO_MAVEN; then
    obs-to-maven -p -d -c "$CACHE_DIR" "$CONFIG_FILE" "$REPOSITORY_DIR"
else
    echo "Skipping obs-to-maven since configuration hasn't changed since last run"
fi

# Hack for struts test case

ST_GROUP=strutstest
ST_ARTIFACT=strutstest
ST_VERSION=1.0.0

ST_URL="https://github.com/uyuni-project/strutstestcase/releases/download/v$ST_VERSION-uyuni/$ST_ARTIFACT-uyuni-$ST_VERSION.jar"
ST_ARTIFACT_DIR="$REPOSITORY_DIR/$ST_GROUP/$ST_ARTIFACT"
ST_FULL_DIR="$ST_ARTIFACT_DIR/$ST_VERSION"

if [ ! -f "$ST_FULL_DIR/$ST_ARTIFACT-$ST_VERSION.jar" ]; then
    if [ ! -d "$ST_FULL_DIR" ]; then
        mkdir -p "$ST_FULL_DIR"
    fi

    curl -L -o "$ST_FULL_DIR/$ST_ARTIFACT-$ST_VERSION.jar" $ST_URL
    sha1sum "$ST_FULL_DIR/$ST_ARTIFACT-$ST_VERSION.jar" | awk '{print $1}' > "$ST_FULL_DIR/$ST_ARTIFACT-$ST_VERSION.jar.sha1" 

    cat << EOF > "$ST_FULL_DIR/$ST_ARTIFACT-$ST_VERSION.pom"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>$ST_GROUP</groupId>
  <artifactId>$ST_ARTIFACT</artifactId>
  <version>$ST_VERSION</version>
  <description>POM was created from obs-to-maven</description>
</project>
EOF
    sha1sum "$ST_FULL_DIR/$ST_ARTIFACT-$ST_VERSION.pom" | awk '{print $1}' > "$ST_FULL_DIR/$ST_ARTIFACT-$ST_VERSION.pom.sha1" 

    cat << EOF > "$ST_ARTIFACT_DIR/maven-metadata-local.xml"
<metadata xmlns="http://maven.apache.org/METADATA/1.1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/METADATA/1.1.0 https://maven.apache.org/xsd/repository-metadata-1.1.0.xsd">
  <groupId>$ST_GROUP</groupId>
  <artifactId>$ST_ARTIFACT</artifactId>
  <versioning>
    <release>$ST_VERSION</release>
    <versions>
      <version>$ST_VERSION</version>
    </versions>
    <lastUpdated>$(date +%Y%m%d%H%M%S)</lastUpdated>
  </versioning>
</metadata>
EOF
    sha1sum "$ST_ARTIFACT_DIR/maven-metadata-local.xml" | awk '{print $1}' > "$ST_ARTIFACT_DIR/maven-metadata-local.xml.sha1" 
fi


date +"Dependencies were last updated on %Y-%m-%d %H:%M:%S" > "$EXECUTION_FILE"
