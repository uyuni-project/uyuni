#!/usr/bin/env bash
set -euo pipefail

usage() {
    cat <<'EOF'
Usage: scripts/update-java-path-references.sh [--apply] [--include-untracked]

Updates references for Java project moves:
  1) java/* -> java/spacewalk-java/*
  2) branding -> java/branding
  3) microservices/coco-attestation -> java/coco-attestation
  4) microservices/uyuni-java-common -> java/uyuni-java-common
  5) microservices/uyuni-java-parent -> java/uyuni-java-parent

Default mode is dry-run: prints files that would change.

Options:
  --apply              Write changes to files.
  --include-untracked  Also process untracked files.
  -h, --help           Show this help.
EOF
}

apply_changes=0
include_untracked=0

while [[ $# -gt 0 ]]; do
    case "$1" in
        --apply)
            apply_changes=1
            ;;
        --include-untracked)
            include_untracked=1
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo "Unknown argument: $1" >&2
            usage
            exit 1
            ;;
    esac
    shift
done

repo_root=$(git rev-parse --show-toplevel 2>/dev/null) || {
    echo "Not inside a git repository." >&2
    exit 1
}

declare -a from_values=()
declare -a to_values=()

add_mapping() {
    from_values+=("$1")
    to_values+=("$2")
}

# Explicit move mappings.
add_mapping "microservices/coco-attestation" "java/coco-attestation"
add_mapping "microservices/uyuni-java-common" "java/uyuni-java-common"
add_mapping "microservices/uyuni-java-parent" "java/uyuni-java-parent"
add_mapping "branding/" "java/branding/"

# Map old java/* references to java/spacewalk-java/*.
# Keep 'branding' excluded because java/branding is also a valid new path.
while IFS= read -r entry; do
    [[ "$entry" == "branding" ]] && continue
    add_mapping "java/$entry/" "java/spacewalk-java/$entry/"
    add_mapping "java/$entry" "java/spacewalk-java/$entry"
done < <(find "$repo_root/java/spacewalk-java" -mindepth 1 -maxdepth 1 -printf '%f\n' | sort)

declare -a git_grep_args=(-Ilz)
for pattern in "${from_values[@]}"; do
    git_grep_args+=(-e "$pattern")
done

declare -a files=()
if [[ $include_untracked -eq 1 ]]; then
    # Include tracked and untracked files, excluding ignored files.
    while IFS= read -r -d '' file; do
        if [[ -f "$file" ]]; then
            for pattern in "${from_values[@]}"; do
                if rg -q --fixed-strings -- "$pattern" "$file"; then
                    files+=("$file")
                    break
                fi
            done
        fi
    done < <(git -C "$repo_root" ls-files -z --cached --others --exclude-standard)
else
    while IFS= read -r -d '' file; do
        files+=("$repo_root/$file")
    done < <(git -C "$repo_root" grep "${git_grep_args[@]}" --)
fi

if [[ ${#files[@]} -eq 0 ]]; then
    echo "No files contain old path references."
    exit 0
fi

declare -a changed_files=()

for file in "${files[@]}"; do
    tmp_file=$(mktemp)
    cp "$file" "$tmp_file"

    for i in "${!from_values[@]}"; do
        old=${from_values[$i]}
        new=${to_values[$i]}

        OLD_VALUE="$old" NEW_VALUE="$new" perl -0777 -i -pe '
            s{(?<![A-Za-z0-9_.-])\Q$ENV{OLD_VALUE}\E(?![A-Za-z0-9_.-])}{$ENV{NEW_VALUE}}g;
        ' "$tmp_file"
    done

    if ! cmp -s "$file" "$tmp_file"; then
        changed_files+=("$file")
        if [[ $apply_changes -eq 1 ]]; then
            mv "$tmp_file" "$file"
        else
            rm -f "$tmp_file"
        fi
    else
        rm -f "$tmp_file"
    fi
done

if [[ ${#changed_files[@]} -eq 0 ]]; then
    echo "No files need updates."
    exit 0
fi

if [[ $apply_changes -eq 1 ]]; then
    echo "Updated ${#changed_files[@]} file(s):"
else
    echo "Dry-run: ${#changed_files[@]} file(s) would be updated:"
fi

for changed in "${changed_files[@]}"; do
    echo "  ${changed#$repo_root/}"
done

if [[ $apply_changes -eq 0 ]]; then
    echo ""
    echo "Run again with --apply to write these changes."
fi
