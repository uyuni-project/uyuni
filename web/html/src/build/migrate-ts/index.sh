#!/bin/sh
# set -e

is_debug=1
input_paths="$*"
input_paths_arr=("$*")

printf "%s\n" "${input_paths_arr[@]}"
exit 0

function debug () {
    if (($is_debug)); then echo $@; fi
}

function count () {
    # \c ensures there's no trailing new line
    # See also: https://stackoverflow.com/a/33356339/1470607
    echo "$@\c" | awk 'NF' | wc -l | tr -d ' '
}

echo "looking for flow files"
flow_paths=$(grep -l '@flow' $input_paths)
echo "found $(count $flow_paths) flow file(s)"

echo "migrating flow"
result=$(yarn flow-to-ts $flow_paths)
debug $result

echo "migrating untyped annotated files"
untyped_paths=$( (yarn tsc 2>&1 || true) | grep 'can only be used in TypeScript files' | sed -e 's/\.js.*/.js/' | uniq)
echo 1
echo $untyped_paths
echo 2
echo "$input_paths"
echo 3
# TODO: $input_paths_arr should be a newline joined string
echo "$untyped_paths" | grep -F $input_paths_arr

exit 0
# TODO: Filter by input_paths
echo "found $(count $untyped_paths) untyped annotated file(s)"
# result=$(yarn flow-to-ts $untyped_paths)
# debug $result

echo "done"
