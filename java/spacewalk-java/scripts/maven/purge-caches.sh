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

set -euo pipefail

# Compute the required directories paths
SCRIPT_DIR=$(cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd)
UYUNI_DIR="$(realpath "$SCRIPT_DIR/../../..")"

ALSO_TARGET=false
ALSO_IVY=false

FORCE=false

usage() {
    cat <<'EOF'
Usage: purge-caches.sh [options]

Clean the development environment by removing cached dependencies
and intermediate build products.

Options:
  -f, --force        Perform deletions (default is dry-run)
  -i, --also-ivy     Also clean Ivy cache and Ant/Ivy build products
  -t, --also-target  Also clean Maven target directories
  -h, --help         Show this help
EOF
}

delete_path() {
    local path="$1"

    if [[ "$FORCE" == true ]]; then
        rm -rf -- "$path"
    fi
}

# Parse command line arguments
while (($#)); do
    case "$1" in
        -i|--also-ivy)
            ALSO_IVY=true
            ;;
        -t|--also-target)
            ALSO_TARGET=true
            ;;
        -f|--force)
            FORCE=true
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        --)
            # End of all options
            shift;
            break
            ;;
        *)
            echo "Unknown option: $1" >&2
            usage
            exit 1
            ;;
    esac
    shift
done

# Simple check to warn the user if an IDE process is running, as it might interfere with the cache purging
if command -v pgrep &> /dev/null; then
    if pgrep -u "$USER" -i -f "\<(idea|code|eclipse)\>" &> /dev/null; then
        echo "** An IDE process is running. It's recommended to close any IDE before purging the caches **"
    fi
fi

if [ "$FORCE" == false ]; then
    echo "Dry run, use the -f or --force option to actually perform the deletions."
fi

echo "Wipe the obs-to-maven cache"
delete_path "$UYUNI_DIR/java/.obs-to-maven-cache/"
echo "Remove the Maven local repository"
delete_path "${HOME:?}/.m2/"
echo "Remove the Maven spacewalk-java repository"
delete_path "$UYUNI_DIR/java/repository/"

if [ "$ALSO_TARGET" == true ]; then
    echo "Wipe the Maven target directories"
    # Find all directories containing a pom.xml and remove the target subdirectory if it exists
    find "$UYUNI_DIR" -name "pom.xml" -print0 | while IFS= read -r -d '' pom; do
        target_dir="$(dirname "$pom")/target"
        if [[ -d "$target_dir" ]]; then
            delete_path "$target_dir"
        fi
    done
fi

if [ "$ALSO_IVY" == true ]; then
    echo "Wipe the Ivy cache"
    delete_path "${HOME:?}/.ivy2/"
    echo "Remove the local Ivy repository"
    delete_path "$UYUNI_DIR/java/buildconf/ivy/repository/"
    echo "Wipe the Ivy resolved libraries"
    delete_path "$UYUNI_DIR/java/lib/"
    echo "Clean ant build files"
    delete_path "$UYUNI_DIR/java/build/"
fi
