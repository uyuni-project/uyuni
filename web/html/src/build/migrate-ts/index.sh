#!/bin/sh
set -e

input_paths="$*"

echo "looking for flow files"
flow_paths=$(grep -l '@flow' $input_paths)
n_flow_paths=$(echo "$flow_paths\c" | awk 'NF' | wc -l | tr -d ' ')
echo "found $n_flow_paths flow files"
# target_paths=$(node build/migrate-ts/find-flow.js $input_paths --no-verbose)

# \c ensures there's no trailing new line
# See also: https://stackoverflow.com/a/33356339/1470607
# n_paths=$(echo "$target_paths\c" | awk 'NF' | wc -l | tr -d ' ')
# echo "found $n_paths files to migrate:"
# echo "$target_paths"

# echo "migrating flow"
# result=$(yarn flow-to-ts $target_paths)
# echo $result

# echo "migrating untyped-annotated"