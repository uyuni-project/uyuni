#! /bin/bash
set -e
#
# For all packages prepared by build-packages-for-obs.sh in
# $WORKSPACE/SRPMS/<package> prepare and submitt changed packages
# to OBS.
#
# Use $OSCRC ot pass an osc configfile containing required credentials
# (otherwise ~/.oscrc)
#
# srpm_package_defs() has a hardcoded list of packages excluded by default.
#
WORKSPACE=${WORKSPACE:-/tmp/push-packages-to-obs}
PACKAGE="$@"
OSCRC=${OSCRC:+-c $OSCRC}

OSC="osc $OSCRC -A https://api.suse.de"
OBS_PROJ=${OBS_PROJ:-Devel:Galaxy:Server:Manager:T}

FAKE_COMITTOBS=${FAKE_COMITTOBS:+1}

grep -v -- "\(--help\|-h\|-?\)\>" <<<"$@" || {
  cat <<EOF
Usage: push-packages-to-obs.sh [PACKAGE]..
Submitt changed packages from \$WORKSPACE/SRPMS/<package> ($WORKSPACE)
to OBS ($OBS_PROJ). Without argument all packages in SRPMS are processed.
EOF
  exit 0
}

function srpm_package_defs() {
  # - "PKG_NAME" from $SRPM_DIR, using a hardcoded blacklist
  # of packages we do not submitt.
  # - define $PACKAGE to build a specific set of packages.
  # - usage:
  #      while read PKG_NAME; do
  #        ...
  #      done < <(srpm_package_defs)
  #
  test -n "$PACKAGE" || {
    PACKAGE=$(find "$SRPM_DIR" -mindepth 1 -maxdepth 1 -type d -printf "%P\n" \
              | grep -v -x -e heirloom-pkgtools -e oracle-server-admin -e oracle-server-scripts -e rhnclient -e smartpm)
  }
  for N in $PACKAGE; do
    test -d "$SRPM_DIR/$N" || {
      echo "No package dir '$SRPM_DIR/$N'" >&2
      exit 99
    }
    echo "$N"
  done
}

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

function log_and_add_failure() {
  test $# -ge 1 || { echo "log_and_add_failure: Wrong args $#: $@" >&2; return 1; }
  local pkg_name="$1"
  local opt_msg="$2"
  FAILED_CNT=$(($FAILED_CNT+1))
  FAILED_PKG="$FAILED_PKG$(echo -ne "\n    $pkg_name${opt_msg:+ ($opt_msg)}")"
  echo "*** FAILED${opt_msg:+ ($opt_msg)} [$pkg_name]"
}

# go..
cd "$WORKSPACE"
T_LOG="$WORKSPACE/tmplog"
trap "test -f \"$T_LOG\" && /bin/rm -rf -- \"$T_LOG\" " 0 1 2 3 13 15

SRPM_DIR="SRPMS"
test -d "$SRPM_DIR" || {
  echo "No'$SRPM_DIR' dir to process." >&2
  exit 99
}
rm -rf "$OBS_PROJ"

echo "Going to update $OBS_PROJ from $SRPM_DIR..."
UNCHANGED_CNT=0
SUCCEED_CNT=0
SUCCEED_PKG=
FAILED_CNT=0
FAILED_PKG=

while read PKG_NAME; do
  echo "=== Processing package [$PKG_NAME]"

  # prepare the srpm dir
  SRPM_PKG_DIR="$SRPM_DIR/$PKG_NAME"
  test -d "$SRPM_PKG_DIR" || {
    log_and_add_failure "$PKG_NAME" "no srpm dir"
    continue
  }

  # Provide a proper release number;
  # - 1.git.a0e2924efdff87699b2989a1c92925b05586aac1%{?dist}
  # + 1%{?dist}%{?!dist:.A}.<RELEASE>
  sed -i '/^Release:/s/\(\.git\..\{40\}\)\?\(%{?dist}\)\?[[:space:]]*$/%{?dist}%{?!dist:.A}.<RELEASE>/' "$SRPM_PKG_DIR/$PKG_NAME.spec" || {
    log_and_add_failure "$PKG_NAME" "inject %{?!dist:.A}.<RELEASE>"
  }

  test -z "$FAKE_COMITTOBS" || {
    log_and_add_failure "$PKG_NAME" "FAKE: Not comitting to OBS"
    continue
  }

  # update from obs (create missing package on the fly)
  OBS_PKG_DIR="$OBS_PROJ/$PKG_NAME"
  rm -rf "$OBS_PKG_DIR"
  $OSC co -u "$OBS_PROJ" "$PKG_NAME" 2>"$T_LOG" || {
    if grep 'does not exist in project' "$T_LOG"; then
      ( set -e; cd "$OBS_PROJ"; $OSC mkpac "$PKG_NAME"; )
    else
      cat "$T_LOG"
      log_and_add_failure "$PKG_NAME" "checkout"
      continue
    fi
  }

  if copy_changed_package "$SRPM_PKG_DIR" "$OBS_PKG_DIR"; then
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
	false
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
  rm -rf "$SRPM_PKG_DIR"
  rm -rf "$OBS_PKG_DIR"
done < <(srpm_package_defs)

echo "======================================================================"
echo "Updated packages:   $SUCCEED_CNT"
echo "Unchanged packages: $UNCHANGED_CNT"
test $FAILED_CNT != 0 && {
  echo "Failed packages:    $FAILED_CNT$FAILED_PKG"
}
echo "======================================================================"

exit $FAILED_CNT
