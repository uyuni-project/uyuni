#! /bin/bash

set -x

SAVE_BRANCHNAMES=(master-weblate)
GIT_ROOT_DIR=$(git rev-parse --show-toplevel)

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
    for change in `git diff --numstat | awk '{print $1}'`; do
        if [ $change -gt 1 ]; then
            git add -u
            git commit -m "update strings for translations in $CODE_DIR"
            popd
            return 2
        fi
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
    for change in `git diff --numstat | awk '{print $1}'`; do
        if [ $change -gt 1 ]; then
            git add -u
            git commit -m "update strings for translations in $XLIFF_DIR"
            return 2
        fi
    done
    git reset --hard
    return 0
}

PO_DIRS=(backend client/rhel/yum-rhn-plugin client/rhel/mgr-daemon client/rhel/spacewalk-client-tools client/tools/spacewalk-abrt web susemanager spacecmd)
commits=0
save=0

for branchname in ${SAVE_BRANCHNAMES[@]}; do
    if git branch --no-color | grep "* $branchname" >/dev/null; then
        save=1
        break
    fi
done

if [ $save -eq 0 ]; then
    echo "Execute this script only on SAVE branches. Current branch is not declared to be save. Abort"
    exit 1
fi

for podir in ${PO_DIRS[@]}; do
    update_po $podir
    ret=$?
    if [ $ret -eq 1 ]; then
        echo "FAILED to update $podir" >&2
	#exit 1
    elif [ $ret -eq 2 ]; then
        commits=$((commits+1))
    fi
done

XLIFF_DIRS=(java/code/src/com/redhat/rhn/frontend/strings/database java/code/src/com/redhat/rhn/frontend/strings/java \
	java/code/src/com/redhat/rhn/frontend/strings/jsp java/code/src/com/redhat/rhn/frontend/strings/nav \
	java/code/src/com/redhat/rhn/frontend/strings/template )

for xliffdir in ${XLIFF_DIRS[@]}; do
    update_xliff $xliffdir
    ret=$?
    if [ $ret -eq 1 ]; then
        echo "FAILED to update $xliffdir" >&2
	#exit 1
    elif [ $ret -eq 2 ]; then
        commits=$((commits+1))
    fi
done

echo "Commits: $commits"
if [ $commits -gt 0 ]; then
    echo "Changes made. Please push with 'git push origin HEAD'"
fi
