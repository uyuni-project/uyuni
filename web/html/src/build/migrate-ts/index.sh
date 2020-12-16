#!/bin/sh
set -e

input_paths="$*"
target_paths=$(node build/migrate-ts/find-flow.js $input_paths --no-verbose)

# \c ensures there's no trailing new line
# See also: https://stackoverflow.com/a/33356339/1470607
n_paths=$(echo "$target_paths\c" | awk 'NF' | wc -l | tr -d ' ')
echo "found $n_paths files to migrate:"
echo "$target_paths"

echo "migrate: initial flow->ts"
result=$(yarn flow-to-ts $target_paths)