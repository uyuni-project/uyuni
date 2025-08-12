#!/usr/bin/env bash

# Get database migrations from a specified version onwards, inclusive
# Usage: ./get-migrations.sh [min included version]
# Default is 5.0 if not specified

set -euo pipefail

readonly script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly min_version="${1:-5.0}"

echo "-- Using min_version ${min_version}"

cd "$script_dir/../.."

find schema/spacewalk/upgrade/ -type f -path '*/susemanager-schema-*to-susemanager-schema-*/*.sql' |
while read -r file; do
  from_version=$(echo "$file" | sed -nE 's|.*/susemanager-schema-([0-9]+\.[0-9]+\.[0-9]+)-to-susemanager-schema-[0-9]+\.[0-9]+\.[0-9]+/.*|\1|p')
  [ -n "$from_version" ] && echo "$from_version $file"
done |
sort -V |
awk -v min="$min_version" '$1 >= min { print $2 }' |
while read -r file; do
  echo "-- $file"
  cat "$file"
  echo ""
done
