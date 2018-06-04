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
OSCAPI=${OSCAPI:-https://api.suse.de}

OSC="osc ${OSCRC} -A ${OSCAPI}"
OBS_PROJ=${OBS_PROJ:-Devel:Galaxy:Manager:TEST}

FAKE_COMITTOBS=${FAKE_COMITTOBS:+1}

# Set KEEP_SRPMS environment variable to TRUE if you want to keep your SRPMS
# Useful if, for example, you are resubmitting the same set to several
# projects in row
KEEP_SRPMS=${KEEP_SRPMS:-FALSE}

DIFF="diff -u"

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
              | grep -v -x -e heirloom-pkgtools -e oracle-server-admin -e oracle-server-scripts -e rhnclient -e smartpm -e jabberd-selinux -e oracle-rhnsat-selinux -e oracle-selinux -e oracle-xe-selinux -e spacewalk-monitoring-selinux -e spacewalk-proxy-selinux -e spacewalk-selinux -e cx_Oracle -e apt-spacewalk -e perl-DBD-Oracle)
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

function tar_diff_p1() {
  local ltar="$1"
  local rtar="$2"
  local tdir="$3"
  test -d "$tdir" || { echo "No tmpdir for tar_diff '$tdir'"; return 1; }

  mkdir "$tdir/L";
  tar_cat "$ltar" | tar xf - -C "$tdir/L" || return 2
  mkdir "$tdir/R";
  tar_cat "$rtar" | tar xf - -C "$tdir/R" || return 2

  if $DIFF -r "$tdir/L"/* "$tdir/R"/*; then
    echo "Content $ltar and $rtar is the same"
    return 0
  else
    echo "Content $ltar and $rtar differs"
    return 1
  fi
}

# Here we have eveyfile (incl. .changes) in git, thus inside the tarball.
# The tarballs rootdirs may differ, as they contain the revision number.
# The specfile also contains the revision number. So do a quick check
# for different .changes, then 'tardiff -p1'
function copy_changed_package()
{
  local sdir="$1"
  test -d "$sdir" || { echo "No source dir '$sdir'" >&2; return 2; }
  local tdir="$2"
  test -d "$tdir" || { echo "No target dir '$tdir'" >&2; return 2; }

  # track changes so we can later decide whether we must tar_diff.
  local diffs=0
  local ttar=""

  # check excess target files (except new tarball version)
  for F in "$tdir"/*; do
    local stem="$(basename "$F")"
    case "$stem" in
      *.tar.*|*.tar|*.tgz|*.tbz2)
        # tarball diff or rename not necessarily implies content change!
        ttar="$tdir/$stem"
        ;;
      *-rpmlintrc)
	# ignore rpmlintrc files
	continue
	;;
      *)
	test -f "$sdir/$stem" || {
	  rm -f "$F"
	  diffs=1
	}
        ;;
    esac
  done

  if [ $diffs == 1 ]; then
    test -z "$ttar" || rm "$ttar"
    cp "$sdir"/* "$tdir"
    return 0
  fi

  # check non-tarball changes
  local star=""
  for F in "$sdir"/*; do
    local stem="$(basename "$F")"
    case "$stem" in
      *.tar.*|*.tar|*.tgz|*.tbz2)
        # tarball diff or rename not necessarily implies content change!
        star="$sdir/$stem"
        ;;
      *)
        if [ -f "$tdir/$stem" ]; then
	  # In sec files ignore Source and %setup lines containing
	  # '-git-<revision>'.
	  #   Source0:      MessageQueue-git-4a9144649ae82fab60f4f11b08c75d46275f47bf.tar.gz
	  #   %setup -q -n MessageQueue-git-4a9144649ae82fab60f4f11b08c75d46275f47bf
	  #
	  $DIFF -I '^\(Source\|%setup\).*-git-' "$tdir/$stem" "$F" || {
	    diffs=1
	    ls -l "$tdir/$stem" "$F"
	    break
	  }
	else
	  # new source file
	  diffs=1
	  break
	fi
        ;;
    esac
  done

  if [ $diffs == 1 -o "${star:+1}" != "${ttar:+1}" ]; then
    test -z "$ttar" || rm "$ttar"
    cp "$sdir"/* "$tdir"
    return 0
  fi
  # HERE: star and ttar are either both present or not

  test -z "$ttar" || {
    # finally do tardiffs
    local tmpd=$(mktemp -d)
    tar_diff_p1 "$ttar" "$star" "$tmpd" || {
      diffs=1
    }
    rm -rf "$tmpd"

    if [ $diffs == 1 ]; then
      test -z "$ttar" || rm "$ttar"
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

  # update from obs (create missing package on the fly)
  for tries in 1 2 3; do
    echo "Try: $tries"
    OBS_PKG_DIR="$OBS_PROJ/$PKG_NAME"
    rm -rf "$OBS_PKG_DIR"
    $OSC co -u "$OBS_PROJ" "$PKG_NAME" 2>"$T_LOG" || {
      if grep 'does not exist in project' "$T_LOG" || grep '404: Not Found' "$T_LOG"; then
        test -d "$OBS_PROJ" || ( mkdir "$OBS_PROJ"; cd "$OBS_PROJ"; $OSC init "$OBS_PROJ"; )
        ( set -e; cd "$OBS_PROJ"; $OSC mkpac "$PKG_NAME"; )
	break
      elif [ $tries -eq 3 ]; then
        cat "$T_LOG"
        log_and_add_failure "$PKG_NAME" "checkout"
        continue 2
      fi
      continue
    }
    for F in "$OBS_PKG_DIR"/*; do
      test -e "$F" || continue
      test -s "$F" || test $tries -eq 3 || continue 2
      test -s "$F" || {
        log_and_add_failure "$PKG_NAME" "zero size file in checkout : $F"
        continue 3
      }
    done
    break
  done


  test -z "$FAKE_COMITTOBS" || {
    echo "FAKE: Not comitting to OBS..."
    continue
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
      log_and_add_failure "$PKG_NAME" "${FAKE_COMITTOBS:+fake }checkin"
      continue
    }
    SUCCEED_CNT=$(($SUCCEED_CNT+1))
    SUCCEED_PKG="$SUCCEED_PKG$(echo -ne "\n    $PKG_NAME")"
  else
    echo "Package is unchanged."
    UNCHANGED_CNT=$(($UNCHANGED_CNT+1))
  fi
  if [ "${KEEP_SRPMS}" == "FALSE" ]; then
    rm -rf "$SRPM_PKG_DIR"
  fi
  rm -rf "$OBS_PKG_DIR"
done < <(srpm_package_defs)

echo "======================================================================"
echo "Unchanged packages: $UNCHANGED_CNT"
echo "Updated packages:   $SUCCEED_CNT$SUCCEED_PKG"
test $FAILED_CNT != 0 && {
  echo "Failed packages:    $FAILED_CNT$FAILED_PKG"
}
echo "======================================================================"

exit $FAILED_CNT
