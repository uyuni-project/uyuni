#! /bin/bash
set -e

function ERREXIT() {
  echo "Error: $@" >&2
  exit 1
}

WORKSPACE=${WORKSPACE:-/tmp/push-packages-to-ibs}
OSCRC=${OSCRC:+-c $OSCRC}

GIT_URL="git://git.suse.de/galaxy/spacewalk.git"
GIT_BRANCH="Manager"

OSC="osc $OSCRC -A https://api.suse.de"
OBS_PROJ="Devel:Galaxy:Server:Manager:T"


# show settings on any arg passed on the cmdline
test -n "$1" && {
cat <<EOF
     GIT_URL="$GIT_URL"
  GIT_BRANCH="$GIT_BRANCH"
         OSC="$OSC"
    OBS_PROJ="$OBS_PROJ"
   WORKSPACE="$WORKSPACE"
EOF
  exit 1
}

test -d "$WORKSPACE" || mkdir -p "$WORKSPACE"
cd "$WORKSPACE"
pwd

# Update from GIT
GIT_DIR="spacewalk"
if true; then
  test -d "$GIT_DIR" || git clone "$GIT_URL" "$GIT_DIR"
  (
    set -e
    cd "$GIT_DIR"
    git fetch
    git reset -q --hard HEAD
    if git show-ref -q "heads/$GIT_BRANCH"; then
	git checkout -q "$GIT_BRANCH"
    else
	git checkout -q -t -b "$GIT_BRANCH" "origin/$GIT_BRANCH"
    fi
    git pull -q
  )
fi

# Test whether git HEAD has moved since last check
GIT_LAST="spacewalk.last"
GIT_LAST_HEAD=
GIT_CUR_HEAD="$(cat $GIT_DIR/.git/refs/heads/Manager)"
if [ -f "$GIT_LAST" ]; then
  GIT_LAST_HEAD="$(cat "$GIT_LAST")"
  if [ "$GIT_LAST_HEAD" == "$GIT_CUR_HEAD" ]; then
    echo "Current branch $GIT_BRANCH is still at $GIT_LAST_HEAD"
    echo "Nothing to do."
    exit 0
  else
    echo "Current branch $GIT_BRANCH was last seen at $GIT_LAST_HEAD"
    echo "Current branch $GIT_BRANCH has changed to   $GIT_CUR_HEAD"
  fi
else
  echo "Current branch $GIT_BRANCH was never seen before."
  echo "Current branch $GIT_BRANCH is now at $GIT_CUR_HEAD"
fi
echo "Going to build new .src.rpm packages..."

# First build the src rpms
SRPM_DIR="SRPMS"
if true; then
  rm -rf "$SRPM_DIR"
  mkdir -p "$SRPM_DIR"

  # some env vars to overwrite make vars (thus make -e)
  export DIST=".Manager"
  export RPMBUILD_BASEDIR="$WORKSPACE/$SRPM_DIR"
  MAKE="make -e"

  SUCCEED_CNT=0
  FAILED_CNT=0
  FAILED_PKG=
  for PKG_DEF in "$GIT_DIR"/rel-eng/packages/*; do
    PKG_NAME=$(basename "$PKG_DEF")
    eval $(awk '{printf "PKG_VER=%s\nPKG_DIR=%s", $1, $2}' $PKG_DEF)
    echo "=== [$PKG_NAME] =================================================="
    if $MAKE -C "$GIT_DIR/$PKG_DIR" test-srpm >log 2>&1; then
      SUCCEED_CNT=$(($SUCCEED_CNT+1))
    else
      FAILED_CNT=$(($FAILED_CNT+1))
      FAILED_PKG="$FAILED_PKG$(echo -ne "\n    $PKG_NAME")"
      cat log
      echo "*** FAILED [$PKG_NAME] =================================================="
    fi
  done
  echo "======================================================================"
  echo "Built Packages:  $SUCCEED_CNT"
  test $FAILED_CNT != 0 && {
    echo "Failed Packages: $FAILED_CNT$FAILED_PKG"
  }
  echo "======================================================================"
fi

# checking for changed packages
#
function unrpm_to.d()
{
  local srpm="$1"
  test -f "$srpm" || { echo "No .rpm to unpack: '$srpm'" >&2; return 1; }
  local tdir="$srpm".d
  rm -rf "$tdir"
  mkdir -p "$tdir"
  ( set -e; cd "$tdir"; unrpm ../"$(basename "$srpm")"; ) >/dev/null 2>&1
  echo "$tdir"
}

function tar_cat() {
    case "$1" in
      *.gz|*.tgz)   gzip -dc "$1" ;;
      *.bz2|*.tbz2) bzip2 -dc "$1" ;;
      *)            cat "$1" ;;
    esac
}

function tar_diff() {
  local ltar="$1"
  local rtar="$2"
  local tdir="$3"
  test -d "$tdir" || { echo "No tmpdir for tar_diff '$tdir'"; return 1; }

  mkdir "$tdir/L";
  tar_cat "$ltar" | tar xf - -C "$tdir/L" || return 2
  mkdir "$tdir/R";
  tar_cat "$rtar" | tar xf - -C "$tdir/R" || return 2

  if diff -r -q "$tdir/L" "$tdir/R"; then
    echo "Content $ltar and $rtar is the same"
    return 0
  else
    echo "Content $ltar and $rtar differs"
    return 1
  fi
}

function copy_changed_package()
{
  local sdir="$1"
  test -d "$sdir" || { echo "No source dir '$sdir'" >&2; return 2; }
  local tdir="$2"
  test -d "$tdir" || { echo "No target dir '$tdir'" >&2; return 2; }

  # track changes so we can later decide whether we must tar_diff.
  local diffs=0

  # check excess target files (usually new tarball version)
  for F in "$tdir"/*; do
    local stem="$(basename "$F")"
    test -f "$sdir/$stem" || {
      rm -f "$F"
      diffs=1
    }
  done

  if [ $diffs == 1 ]; then
    cp "$sdir"/* "$tdir"
    return 0
  fi

  # check non-tarball changes
  local tardiff=""
  for F in "$sdir"/*; do
    local stem="$(basename "$F")"
    test -f "$tdir/$stem" || {
      # new source file
      diffs=1
      break
    }
    diff -q "$F" "$tdir/$stem" && {
      # no changes
      continue
    }
    case "$stem" in
      *.tar.*|*.tar|*.tgz|*.tbz2)
        # tarball diff not necessarily implies content change
        tardiff="$tardiff $stem"
        ;;
      *)
        diffs=1
        break
        ;;
    esac
  done

  if [ $diffs == 1 ]; then
    cp "$sdir"/* "$tdir"
    return 0
  fi

  # finally do tardiffs
  test -n "$tardiff" && {
    for stem in $tardiff; do
      local tmpd=$(mktemp -d)
      tar_diff "$sdir/$stem" "$tdir/$stem" "$tmpd" || {
        rm -rf "$tmpd"
        diffs=1
        break
      }
      rm -rf "$tmpd"
    done

    if [ $diffs == 1 ]; then
      cp "$sdir"/* "$tdir"
      return 0
    fi
  }
  # No changes
  return 1
}

if true; then
  echo "Going to load obs packages..."
  rm -rf "$OBS_PROJ"
  $OSC co -u "$OBS_PROJ"

  echo "Going to update changed obs packages..."
  for SRPM in "$SRPM_DIR"/rpmbuild-*-"$GIT_CUR_HEAD"/*.src.rpm; do
    test -f "$SRPM" || continue
    PKG_NAME=$(rpm -qp "$SRPM" --qf '%{name}')
    echo "=== [$PKG_NAME] =================================================="

    SRPM_D=$(unrpm_to.d "$SRPM")

    OBS_PKG_DIR="$OBS_PROJ/$PKG_NAME"
    test -d "$OBS_PKG_DIR" || (
      set -e
      cd "$OBS_PROJ"
      $OSC mkpac "$PKG_NAME"
    )

    copy_changed_package "$SRPM_D" "$OBS_PKG_DIR" && (
      set -e
      echo "Package has changed."
      cd "$OBS_PKG_DIR"
      $OSC addremove
      $OSC ci -m "Git submitt $GIT_BRANCH($GIT_CUR_HEAD)"
    )
  done
fi

# summary
echo "======================================================================"
echo "Built Packages:  $SUCCEED_CNT"
test $FAILED_CNT != 0 && {
  echo "Failed Packages: $FAILED_CNT$FAILED_PKG"
}
echo "======================================================================"
