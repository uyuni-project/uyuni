#! /bin/bash

set -x

SAVE_BRANCHNAMES=(master-weblate)


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

PO_DIRS=(backend client/rhel/yum-rhn-plugin client/rhel/mgr-daemon client/rhel/spacewalk-client-tools client/tools/spacewalk-abrt web susemanager)
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
echo "Commits: $commits"
if [ $commits -gt 0 ]; then
    echo "Changes made. Please push with 'git push origin HEAD'"
fi
