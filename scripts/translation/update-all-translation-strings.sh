#! /bin/bash

set -x
export LC_ALL=C

SAFE_BRANCHNAMES=(master-weblate new-translation-strings)
SAFE_BRANCHNAMES+=($ADDITIONAL_SAFE_BRANCHNAME)
GIT_ROOT_DIR=$(git rev-parse --show-toplevel)

function get_copyrights() {
    FILE="$1"

    git log --follow --date=format:%Y \
        --grep "^Translated using Weblate" \
        --pretty="format:%ad;%an;%ae" -- "$FILE" \
        | sort | uniq \
        | awk -F';' '
            BEGIN {
                SUSE_KEY = "SUSE LLC";
                SUSE_DOMAIN = "@suse.com";
            }
            {
                year = $1;
                author_name = $2;
                author_email = $3;

                if (author_email ~ SUSE_DOMAIN "$") {
                    # Emails ending in @suse.com go under the SUSE_KEY
                    key = SUSE_KEY;
                } else {
                    # Other authors use their name and email as the key
                    key = author_name " <" author_email ">";
                }

                if (!start[key] || year < start[key]) start[key] = year;
                if (!end[key]   || year > end[key])   end[key]   = year;
            }
            END {
                # Print the results
                for (k in start) {
                    # Check if the key is the special SUSE key
                    if (k == SUSE_KEY) {
                        author_line = "SUSE LLC";
                    } else {
                        author_line = k; # Original author format
                    }

                    if (start[k] == end[k])
                        printf "%s %s\n", start[k], author_line;
                    else
                        printf "%s-%s %s\n", start[k], end[k], author_line;
                }
            }
        '
}

function update_po() {
    CODE_DIR=$1
    PO_DIR=$CODE_DIR/po
    if [ ! -d $PO_DIR ]; then
        echo "Directory '$PO_DIR' not found" >&2
        return 1;
    fi
    if [ ! -e $PO_DIR/Makefile ]; then
        echo "'$PO_DIR/Makefile' does not exist" >&2
        return 1
    fi
    pushd $PO_DIR
    make update-po
    make clean
    for tfile in `git diff --diff-filter=ACM --numstat | cut -f3 | grep "\.po"`; do
        # Remove the previous comment
        sed '/SPDX-FileCopyrightText:/d; /^# Copyright /d; /FIRST AUTHOR/d' -i $tfile
        sed 's/^# This file is distributed under the same license.*$/# SPDX-License-Identifier: GFDL-1.2-only/' -i $tfile

        # Add the updated one
        copyrights=$(get_copyrights $tfile | sed 's/^/# /' | sed 's/\\/\\\\/')
        { echo -e "#\n$copyrights\n#"; } | sed 's/\\/\\\\/g' | sed $tfile -e "1r /dev/stdin"
    done
    # we ignore changes in location (#: ) and the POT-Creation-Date change
    for change in `git diff --ignore-matching-lines="#: " --ignore-matching-lines="POT-Creation-Date" --numstat | awk '{print $1}'`; do
        git add -u
        git commit -m "update strings for translations in $CODE_DIR"
        popd
        return 2
    done
    git reset --hard
    popd
    return 0
}

function update_xliff() {
    XLIFF_DIR=$1
    if [ ! -d $GIT_ROOT_DIR/$XLIFF_DIR ]; then
        echo "Directory '$XLIFF_DIR' not found" >&2
        return 1;
    fi
    $GIT_ROOT_DIR/scripts/translation/xliffmerger.py $GIT_ROOT_DIR/$XLIFF_DIR
    if [ $? -ne 0 ]; then
        echo "xliffmerger returned a fault code"
        return 1
    fi
    for tfile in $GIT_ROOT_DIR/$XLIFF_DIR/* ; do
        sed -i '1s/<?xml .*/<?xml version="1.0" encoding="UTF-8"?>/' $tfile
        sed -i 's/ \/>/\/>/g' $tfile
	    if [ -n "$(tail -c -1 "$tfile")" ]; then
            echo >> $tfile
        fi
        # Remove the previous comment
        sed '/<!--/{:1;/-->/!{N;b1};/SPDX-FileCopyrightText:/d}' -i $tfile

        # Add the updated one
        copyrights=$(get_copyrights $tfile | sed 's/^/ ~ /' | sed 's/\\/\\\\/')
        comment=$(printf '<!--\n%s\n ~ SPDX-License-Identifier: GFDL-1.2-only\n-->\n' "$copyrights")
        { echo -e "$comment"; } | sed 's/\\/\\\\/g' | sed -i $tfile -e "/<?xml/r /dev/stdin"
    done
    MODIFIED=`git status --short --porcelain --untracked-files=no | wc -l`
    if [ $MODIFIED -gt 0 ]; then
        git add -u
        git commit -m "update strings for translations in $XLIFF_DIR"
        return 2
    fi
    git reset --hard
    return 0
}

PO_DIRS=(python/spacewalk client/rhel/spacewalk-client-tools web susemanager spacecmd)
commits=0
safe=0

for branchname in ${SAFE_BRANCHNAMES[@]}; do
    if git branch --no-color | grep "* $branchname" >/dev/null; then
        safe=1
        break
    fi
done

if [ $safe -eq 0 ]; then
    echo "Execute this script only on SAFE branches. Current branch is not declared to be safe. Abort"
    exit 1
fi

for podir in ${PO_DIRS[@]}; do
    update_po $podir
    ret=$?
    if [ $ret -eq 1 ]; then
        echo "FAILED to update $podir" >&2
	exit 1
    elif [ $ret -eq 2 ]; then
        commits=$((commits+1))
    fi
done

XLIFF_DIRS=(java/core/src/main/resources/com/redhat/rhn/frontend/strings/database java/core/src/main/resources/com/redhat/rhn/frontend/strings/java \
	java/core/src/main/resources/com/redhat/rhn/frontend/strings/jsp java/core/src/main/resources/com/redhat/rhn/frontend/strings/nav \
	java/core/src/main/resources/com/redhat/rhn/frontend/strings/template )

for xliffdir in ${XLIFF_DIRS[@]}; do
    update_xliff $xliffdir
    ret=$?
    if [ $ret -eq 1 ]; then
        echo "FAILED to update $xliffdir" >&2
	exit 1
    elif [ $ret -eq 2 ]; then
        commits=$((commits+1))
    fi
done

echo "Commits: $commits"
if [ $commits -gt 0 ]; then
    echo "Changes made. Please push with 'git push origin HEAD'"
fi
