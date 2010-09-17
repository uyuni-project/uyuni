#! /bin/bash
set -e
#
# For all packages in git:/rel-eng/packages (or defined in $PACKAGES):
# - pull from git
# - build srpms
# - submitt to obs
#
# git_package_defs() has a hardcoded list of packages excluded by default.
#
WORKSPACE=${WORKSPACE:-/tmp/push-packages-to-obs}
OSCRC=${OSCRC:+-c $OSCRC}

GIT_URL="git://git.suse.de/galaxy/spacewalk.git"
GIT_BRANCH="Manager"

OSC="osc $OSCRC -A https://api.suse.de"
OBS_PROJ="Devel:Galaxy:Server:Manager:T"

# some fakes for testing
FAKE_PULLFROMGIT=
FAKE_BUILDSRPMS=
FAKE_UPDATEOBS=
FAKE_COMITTOBS=1


function pull_from_git()
{
  test $# == 3 || { echo "pull_from_git: Wrong args $#: $@" >&2; return 1; }
  local git_dir="$1"
  local git_url="$2"
  local git_branch="$3"

  test -d "$git_dir" || git clone "$git_url" "$git_dir"

  pushd "$git_dir"
  git fetch
  git reset -q --hard HEAD
  if git show-ref -q "heads/$git_branch"; then
      git checkout -q "$git_branch"
  else
      git checkout -q -t -b "$git_branch" "origin/$git_branch"
  fi
  git pull -q
  popd
}

# create workspace
test -d "$WORKSPACE" || mkdir -p "$WORKSPACE"
cd "$WORKSPACE"

# save a copy of stdout/stderr in lastrun.log
exec > >(tee lastrun.log) 2>&1

# show basic settings
cat <<EOF
     GIT_URL="$GIT_URL"
  GIT_BRANCH="$GIT_BRANCH"
         OSC="$OSC"
    OBS_PROJ="$OBS_PROJ"
   WORKSPACE="$WORKSPACE"
EOF

# Update git
GIT_DIR="$WORKSPACE/git_dir"

if [ -z "$FAKE_PULLFROMGIT" ]; then
  pull_from_git "$GIT_DIR" "$GIT_URL" "$GIT_BRANCH"
else
  echo "FAKE: Reusing existing git checkout..."
fi

# Test whether git HEAD has moved since last check
GIT_LAST="$WORKSPACE/git_last"
GIT_LAST_HEAD="$(test ! -f "$GIT_LAST" || cat "$GIT_LAST")"
GIT_CURR_HEAD="$(cat $GIT_DIR/.git/refs/heads/"$GIT_BRANCH")"

if [ -n "$GIT_LAST_HEAD" ]; then
  if [ "$GIT_LAST_HEAD" == "$GIT_CURR_HEAD" ]; then
    echo "Current branch $GIT_BRANCH is still at $GIT_LAST_HEAD"
    echo "Nothing to do."
    exit 0
  else
    echo "Current branch $GIT_BRANCH was last seen at $GIT_LAST_HEAD"
    echo "Current branch $GIT_BRANCH has changed to   $GIT_CURR_HEAD"
  fi
else
  echo "Current branch $GIT_BRANCH was never seen before."
  echo "Current branch $GIT_BRANCH is now at $GIT_CURR_HEAD"
fi

function git_package_defs() {
  # - "PKG_NAME PKG_VER PKG_DIR" from git:/rel-eng/packages/, using
  #   a hardcoded blacklist of packages we do not build.
  # - define $PACKAGE to build a specific set of packages.
  # - usage:
  #      while read PKG_NAME PKG_VER PKG_DIR; do
  #        ...
  #      done < <(git_package_defs)
  #
  test -n "$PACKAGE" || {
    PACKAGE=$(ls "$GIT_DIR"/rel-eng/packages/ \
              | grep -v -x -e heirloom-pkgtools -e oracle-server-admin -e oracle-server-scripts -e rhnclient -e smartpm)
  }
  for N in $PACKAGE; do
    awk -vN=$N '{printf "%s %s %s\n", N, $1, $2}' "$GIT_DIR"/rel-eng/packages/$N
  done
}

function log_and_add_failure() {
  test $# -ge 1 || { echo "log_and_add_failure: Wrong args $#: $@" >&2; return 1; }
  local pkg_name="$1"
  local opt_msg="$2"
  FAILED_CNT=$(($FAILED_CNT+1))
  FAILED_PKG="$FAILED_PKG$(echo -ne "\n    $pkg_name${opt_msg:+ ($opt_msg)}")"
  echo "*** FAILED${opt_msg:+ ($opt_msg)} [$pkg_name] =================================================="
}

# First build the src rpms
SRPM_DIR="SRPMS"

# some env vars to overwrite make vars (thus make -e)
# (alternative to use a ~/.spacewalk-build-rc)
export DIST=".Manager"
export RPMBUILD_BASEDIR="$WORKSPACE/$SRPM_DIR"
MAKE="make -e"

if [ -z "$FAKE_BUILDSRPMS" ]; then
  echo "Going to build new .src.rpm packages..."
  rm -rf "$SRPM_DIR"
  mkdir -p "$SRPM_DIR"

  SUCCEED_CNT=0
  FAILED_CNT=0
  FAILED_PKG=

  while read PKG_NAME PKG_VER PKG_DIR; do
    echo "=== [$PKG_NAME] =================================================="
    if $MAKE -C "$GIT_DIR/$PKG_DIR" COMMIT_SHA1="$GIT_CURR_HEAD" srpm >log 2>&1; then
      SUCCEED_CNT=$(($SUCCEED_CNT+1))
    else
      cat log
      log_and_add_failure "$PKG_NAME"
      continue
    fi
  done < <(git_package_defs)

  echo "======================================================================"
  echo "Built .src.rpm packages:  $SUCCEED_CNT"
  test $FAILED_CNT != 0 && {
    echo "Failed .src.rpm packages: $FAILED_CNT$FAILED_PKG"
    test $SUCCEED_CNT -gt 0 || exit 1
  }
  echo "======================================================================"
else
  echo "FAKE: Reusing existing .src.rpm packages..."
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

if [ -z "$FAKE_UPDATEOBS" ]; then
  echo "Going to update changed obs packages..."

  UNCHANGED_CNT=0
  SUCCEED_CNT=0
  SUCCEED_PKG=
  FAILED_CNT=0
  FAILED_PKG=

  while read PKG_NAME PKG_VER PKG_DIR; do
    echo "=== [$PKG_NAME] =================================================="
    SRPM="$SRPM_DIR/rpmbuild-$PKG_NAME-$PKG_VER/$PKG_NAME-$PKG_VER$DIST.src.rpm"
    test -f "$SRPM" || {
      log_and_add_failure "$PKG_NAME" "no srpm"
      continue
    }

    # update from obs
    rm -rf "$OBS_PROJ"
    $OSC co -u "$OBS_PROJ" "$PKG_NAME" 2>log || {
      if grep 'does not exist in project' log; then
	( set -e; cd "$OBS_PROJ"; $OSC mkpac "$PKG_NAME"; )
      else
	cat log
	log_and_add_failure "$PKG_NAME" "checkout"
	continue
      fi
    }

    OBS_PKG_DIR="$OBS_PROJ/$PKG_NAME"
    SRPM_D=$(unrpm_to.d "$SRPM")

    # some specfile checks
    grep '^Release:.*%{?suse_version:%{?!dist:.A}.<RELEASE>}' "$SRPM_D"/*.spec || {
      log_and_add_failure "$PKG_NAME" "missing %{?suse_version:%{?!dist:.A}.<RELEASE>"
      continue
    }

    if copy_changed_package "$SRPM_D" "$OBS_PKG_DIR"; then
      echo "Package has changed, updating..."
      (
	set -e
	cd "$OBS_PKG_DIR"
	$OSC addremove >/dev/null
	$OSC status
	if [ -z "$FAKE_COMITTOBS" ]; then
	  $OSC ci -m "Git submitt $GIT_BRANCH($GIT_CURR_HEAD)"
	else
	  echo "FAKE: Not comitting to OBS..."
	fi
      ) || {
	log_and_add_failure "$PKG_NAME" "checkin"
	continue
      }
      SUCCEED_CNT=$(($SUCCEED_CNT+1))
      SUCCEED_PKG=="$SUCCEED_PKG$(echo -ne "\n    $PKG_NAME")"
    else
      echo "Package is unchanged."
      UNCHANGED_CNT=$(($UNCHANGED_CNT+1))
    fi
  done < <(git_package_defs)

  echo "======================================================================"
  echo "Updated packages:   $SUCCEED_CNT"
  echo "Unchanged packages: $UNCHANGED_CNT"
  test $FAILED_CNT != 0 && {
    echo "Failed packages:    $FAILED_CNT$FAILED_PKG"
    exit 1
  }
  echo "======================================================================"
else
  echo "FAKE: Not uploading to OBS..."
fi
