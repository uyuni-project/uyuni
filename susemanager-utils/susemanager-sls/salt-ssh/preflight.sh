#!/bin/sh

if [ $# -lt 2 ]; then
    echo "Error: Wrong number of arguments!"
    exit 255
fi

# In case the script is executed using different interpreter than bash
# then we call the script explicitely using bash
SH_PATH=$(readlink /proc/$$/exe)
SH_NAME=$(basename "${SH_PATH}")
if ! [ "${SH_NAME}" = "bash" ]; then
  exec bash "$0" "$@"
fi

REPO_HOST=$1
if [[ $2 =~ ^[0-9]+$ ]]; then
   REPO_PORT=$2
else
   echo 'Error: $2 (REPO_PORT) must be an integer.' >&2
   exit 254
fi
FAIL_ON_ERROR=1
if [ "$3" = "1" ]; then
    FAIL_ON_ERROR=0
fi
BOOTSTRAP=0
if [ "$4" = "1" ]; then
    BOOTSTRAP=1
fi
if [ ${BOOTSTRAP} -eq 1 ] && [ ${REPO_PORT} -ne 443 ]; then
    REPO_HOST="localhost"
fi
CLIENT_REPOS_ROOT="https://${REPO_HOST}:${REPO_PORT}/pub/repositories"

VENV_INST_DIR="/usr/lib/venv-salt-minion"
VENV_TMP_DIR="/var/tmp/venv-salt-minion"
VENV_HASH_FILE="venv-hash.txt"

TEMP_DIR=$(mktemp -d -t salt-bundle-XXXXXXXXXX)
trap "popd > /dev/null; rm -rf ${TEMP_DIR}" EXIT
pushd "${TEMP_DIR}" > /dev/null

function exit_with_message_code() {
    echo "$1" >&2
    if [ ${FAIL_ON_ERROR} -ne 0 ]; then
        exit $2
    fi
    exit 0
}

# the order matters: see bsc#1222347
if [ -x /usr/bin/dnf ]; then
    INSTALLER=yum
elif [ -x /usr/bin/yum ]; then
    INSTALLER=yum
elif [ -x /usr/bin/zypper ]; then
    INSTALLER=zypper
elif [ -x /usr/bin/apt ]; then
    INSTALLER=apt
else
    exit_with_message_code "Error: Unable to detect installer on the OS!" 1
fi

if [ -x /usr/bin/wget ]; then
    output=`LANG=en_US /usr/bin/wget --no-check-certificate 2>&1`
    error=`echo $output | grep "unrecognized option"`
    if [ -z "$error" ]; then
        FETCH="/usr/bin/wget -nv -r -nd --no-check-certificate"
    else
        FETCH="/usr/bin/wget -nv -r -nd"
    fi
elif [ -x /usr/bin/curl ]; then
    output=`LANG=en_US /usr/bin/curl -k 2>&1`
    error=`echo $output | grep "is unknown"`
    if [ -z "$error" ]; then
        FETCH="/usr/bin/curl -ksSOf"
    else
        FETCH="/usr/bin/curl -sSOf"
    fi
else
    exit_with_message_code "Error: To be able to download files, please install either 'wget' or 'curl'" 2
fi

if [ "$INSTALLER" == "zypper" ] || [ "$INSTALLER" == "yum" ]; then
    ARCH=$(rpm --eval "%{_arch}")
else
    ARCH=$(dpkg --print-architecture)
fi

function getY_CLIENT_CODE_BASE() {
    local BASE=""
    local VERSION=""
    # SLES ES6 is a special case; it will install a symlink named
    # centos-release pointing to redhat-release which will make the
    # original test fail; reverting the checks does not help as this
    # will break genuine CentOS systems. So use the poor man's approach
    # to detect this special case. SLES ES7 does not have this issue
    # https://bugzilla.suse.com/show_bug.cgi?id=1132576
    # https://bugzilla.suse.com/show_bug.cgi?id=1152795
    if [ -L /usr/share/doc/sles_es-release ]; then
        BASE="res"
        VERSION=6
    elif [ -f /etc/openEuler-release ]; then
        grep -v '^#' /etc/openEuler-release | grep -q '\(openEuler\)' && BASE="openEuler"
	VERSION=`grep -v '^#' /etc/openEuler-release | grep -Po '(?<=release )(\d+\.)+\d+'`
    elif [ -f /etc/almalinux-release ]; then
        grep -v '^#' /etc/almalinux-release | grep -q '\(AlmaLinux\)' && BASE="almalinux"
        VERSION=`grep -v '^#' /etc/almalinux-release | grep -Po '(?<=release )\d+'`
    elif [ -f /etc/rocky-release ]; then
        grep -v '^#' /etc/rocky-release | grep -q '\(Rocky Linux\)' && BASE="rockylinux"
        VERSION=`grep -v '^#' /etc/rocky-release | grep -Po '(?<=release )\d+'`
    elif [ -f /etc/oracle-release ]; then
        grep -v '^#' /etc/oracle-release | grep -q '\(Oracle\)' && BASE="oracle"
        VERSION=`grep -v '^#' /etc/oracle-release | grep -Po '(?<=release )\d+'`
    elif [ -f /etc/alinux-release ]; then
        grep -v '^#' /etc/alinux-release | grep -q '\(Alibaba\)' && BASE="alibaba"
        VERSION=`grep -v '^#' /etc/alinux-release | grep -Po '(?<=release )\d+'`
    elif [ -f /etc/centos-release ]; then
        grep -v '^#' /etc/centos-release | grep -q '\(CentOS\)' && BASE="centos"
        VERSION=`grep -v '^#' /etc/centos-release | grep -Po '(?<=release )\d+'`
    elif [ -f /etc/redhat-release ]; then
        grep -v '^#' /etc/redhat-release | grep -q '\(Red Hat\)' && BASE="res"
        VERSION=`grep -v '^#' /etc/redhat-release | grep -Po '(?<=release )\d+'`
    elif [ -f /etc/os-release ]; then
        BASE=$(source /etc/os-release; echo $ID)
        VERSION=$(source /etc/os-release; echo $VERSION_ID)
    fi
    Y_CLIENT_CODE_BASE="${BASE:-unknown}"
    Y_CLIENT_CODE_VERSION="${VERSION:-unknown}"
}

function getZ_CLIENT_CODE_BASE() {
    local BASE=""
    local VERSION=""
    local PATCHLEVEL=""
    if [ -r /etc/SuSE-release ]; then
        grep -q 'Enterprise' /etc/SuSE-release && BASE='sle'
        eval $(grep '^\(VERSION\|PATCHLEVEL\)' /etc/SuSE-release | tr -d '[:blank:]')
        if [ "$BASE" != "sle" ]; then
            grep -q 'openSUSE' /etc/SuSE-release && BASE='opensuse'
            VERSION="$(grep '^\(VERSION\)' /etc/SuSE-release | tr -d '[:blank:]' | sed -n 's/.*=\([[:digit:]]\+\).*/\1/p')"
            PATCHLEVEL="$(grep '^\(VERSION\)' /etc/SuSE-release | tr -d '[:blank:]' | sed -n 's/.*\.\([[:digit:]]*\).*/\1/p')"
        fi
    elif [ -r /etc/os-release ]; then
        grep -q 'Enterprise' /etc/os-release && BASE='sle'
        if [ "$BASE" != "sle" ]; then
            grep -q 'openSUSE' /etc/os-release && BASE='opensuse'
        fi
        grep -q 'Micro' /etc/os-release && BASE="${BASE}micro"
        VERSION="$(grep '^\(VERSION_ID\)' /etc/os-release | sed -n 's/.*"\([[:digit:]]\+\).*/\1/p')"
        PATCHLEVEL="$(grep '^\(VERSION_ID\)' /etc/os-release | sed -n 's/.*\.\([[:digit:]]*\).*/\1/p')"
        # openSUSE MicroOS
        grep -q 'MicroOS' /etc/os-release && BASE='opensusemicroos' && VERSION='latest'
        # openSUSE Tumbleweed
        grep -q 'Tumbleweed' /etc/os-release && BASE='opensusetumbleweed' && VERSION='latest'
    fi
    Z_CLIENT_CODE_BASE="${BASE:-unknown}"
    Z_CLIENT_CODE_VERSION="${VERSION:-unknown}"
    Z_CLIENT_CODE_PATCHLEVEL="${PATCHLEVEL:-0}"
}

function getA_CLIENT_CODE_BASE() {
    local BASE=""
    local VERSION=""
    local VARIANT_ID=""

    if [ -f /etc/os-release ]; then
        BASE=$(source /etc/os-release; echo $ID)
        VERSION=$(source /etc/os-release; echo $VERSION_ID)
        VARIANT_ID=$(source /etc/os-release; echo $VARIANT_ID)
    fi
    A_CLIENT_CODE_BASE="${BASE:-unknown}"
    local VERCOMPS=(${VERSION/\./ }) # split into an array 18.04 -> (18 04)
    A_CLIENT_CODE_MAJOR_VERSION=${VERCOMPS[0]}
    # Ubuntu only
    if [ "${BASE}" == "ubuntu" ]; then
        A_CLIENT_CODE_MINOR_VERSION=$((${VERCOMPS[1]} + 0)) # convert "04" -> 4
    fi
    A_CLIENT_VARIANT_ID="${VARIANT_ID:-unknown}"
}

if [ "${INSTALLER}" = "yum" ]; then
    getY_CLIENT_CODE_BASE
    CLIENT_REPO_URL="${CLIENT_REPOS_ROOT}/${Y_CLIENT_CODE_BASE}/${Y_CLIENT_CODE_VERSION}/bootstrap"
    # In case of Red Hat derivatives, check if bootstrap repository is available, if not, fallback to RES.
    if [ "$Y_CLIENT_CODE_BASE" == almalinux ] || \
      [ "$Y_CLIENT_CODE_BASE" == rockylinux ] || \
      [ "$Y_CLIENT_CODE_BASE" == oracle ] || \
      [ "$Y_CLIENT_CODE_BASE" == alibaba ] || \
      [ "$Y_CLIENT_CODE_BASE" == centos ]; then
        $FETCH $CLIENT_REPO_URL/repodata/repomd.xml &> /dev/null
        if [ $? -ne 0 ]; then
            CLIENT_REPO_URL="${CLIENT_REPOS_ROOT}/res/${Y_CLIENT_CODE_VERSION}/bootstrap"
        fi
    fi
elif [ "${INSTALLER}" = "zypper" ]; then
    getZ_CLIENT_CODE_BASE
    CLIENT_REPO_URL="${CLIENT_REPOS_ROOT}/${Z_CLIENT_CODE_BASE}/${Z_CLIENT_CODE_VERSION}/${Z_CLIENT_CODE_PATCHLEVEL}/bootstrap"
elif [ "${INSTALLER}" = "apt" ]; then
    getA_CLIENT_CODE_BASE
    if [ "${A_CLIENT_CODE_BASE}" == "debian" ] || [ "${A_CLIENT_CODE_BASE}" == "raspbian" ]; then
        CLIENT_REPO_URL="${CLIENT_REPOS_ROOT}/${A_CLIENT_CODE_BASE}/${A_CLIENT_CODE_MAJOR_VERSION}/bootstrap"
    elif [ "${A_CLIENT_CODE_BASE}" == "astra" ]; then
        CLIENT_REPO_URL="${CLIENT_REPOS_ROOT}/${A_CLIENT_CODE_BASE}/${A_CLIENT_VARIANT_ID}/bootstrap"
    else
        CLIENT_REPO_URL="${CLIENT_REPOS_ROOT}/${A_CLIENT_CODE_BASE}/${A_CLIENT_CODE_MAJOR_VERSION}/${A_CLIENT_CODE_MINOR_VERSION}/bootstrap"
    fi
fi

SELINUX_POLICY_FILENAME="salt_ssh_port_forwarding.cil"
function selinux_policy_loaded {
    semodule -l | grep -x $SELINUX_POLICY_FILENAME
}

# Our SSH tunnel uses a custom port and we must configure SELinux to account for it
if [[ $REPO_HOST == "localhost" ]] && command -v selinuxenabled && selinuxenabled; then
    if ! selinux_policy_loaded; then
        echo "(portcon tcp ${REPO_PORT} (system_u object_r ssh_port_t ((s0)(s0))))" >$SELINUX_POLICY_FILENAME
        if ! semodule -i $SELINUX_POLICY_FILENAME; then
            exit_with_message_code "Error: Failed to install SELinux policy with port=${REPO_PORT}." 7
        fi
    fi
fi

VENV_FILE="venv-enabled-${ARCH}.txt"
VENV_ENABLED_URL="${CLIENT_REPO_URL}/${VENV_FILE}"
$FETCH $VENV_ENABLED_URL > /dev/null 2>&1

if [ -f "${VENV_FILE}" ]; then
    VENV_SOURCE="bootstrap"
else
    if [ "${INSTALLER}" = "apt" ] && dpkg-query -s venv-salt-minion > /dev/null 2>&1 && [ -d "${VENV_INST_DIR}" ]; then
        VENV_SOURCE="dpkg"
    elif rpm -q --quiet venv-salt-minion 2> /dev/null && [ -d "${VENV_INST_DIR}" ]; then
        VENV_SOURCE="rpm"
    fi
fi

if [ -n "${VENV_SOURCE}" ]; then
    if [ "${VENV_SOURCE}" = "bootstrap" ]; then
        VENV_ENABLED=$(cat "${VENV_FILE}")
        VENV_HASH=$(echo "${VENV_ENABLED}" | sed 's/ .*//')
        VENV_PKG_PATH=$(echo "${VENV_ENABLED}" | sed 's/^.* //')
        if [ -z "${VENV_HASH}" ] || [ -z "${VENV_PKG_PATH}" ]; then
            exit_with_message_code "Error: File ${CLIENT_REPO_URL}/${VENV_FILE} is malformed!" 4
        fi
    elif [ "${VENV_SOURCE}" = "rpm" ]; then
        VENV_HASH=$(rpm -qi venv-salt-minion | sha256sum | tr -d '\- ')
    elif [ "${VENV_SOURCE}" = "dpkg" ]; then
        VENV_HASH=$(dpkg -s venv-salt-minion | sha256sum | tr -d '\- ')
    fi
    if [ -f "${VENV_TMP_DIR}/${VENV_HASH_FILE}" ]; then
        if [ -x "${VENV_TMP_DIR}/bin/python" ]; then
            PRE_VENV_HASH=$(cat "${VENV_TMP_DIR}/${VENV_HASH_FILE}")
        else
            rm -f "${VENV_TMP_DIR}/${VENV_HASH_FILE}"
        fi
    fi
    if [ "${VENV_HASH}" != "${PRE_VENV_HASH}" ]; then
        if [ "${VENV_SOURCE}" = "bootstrap" ]; then
            VENV_PKG_URL="${CLIENT_REPO_URL}/${VENV_PKG_PATH}"
            $FETCH $VENV_PKG_URL > /dev/null 2>&1
            VENV_PKG_FILE=$(basename "${VENV_PKG_PATH}")
            if [ ! -f "${VENV_PKG_FILE}" ] && [ -z "${PRE_VENV_HASH}" ]; then
                exit_with_message_code "Error: Unable to download $VENV_PKG_URL file!" 5
            fi
        fi
        rm -rf "${VENV_TMP_DIR}"
        if [ "${VENV_SOURCE}" = "bootstrap" ]; then
            mkdir -p "${VENV_TMP_DIR}"
            pushd "${VENV_TMP_DIR}" > /dev/null
            if [ "${VENV_PKG_FILE##*\.}" = "deb" ]; then
                dpkg-deb -x "${TEMP_DIR}/${VENV_PKG_FILE}" .
                rm -rf etc lib var usr/bin usr/sbin usr/share usr/lib/tmpfiles.d
            else
                rpm2cpio "${TEMP_DIR}/${VENV_PKG_FILE}" | cpio -idm '*/lib/venv-salt-minion/*' >> /dev/null 2>&1
            fi
            mv usr usr.tmp
            mv usr.tmp/lib/venv-salt-minion/* .
            rm -rf usr.tmp
            if [ ! -x bin/python ]; then
                rm -f "${VENV_TMP_DIR}/${VENV_HASH_FILE}"
                exit_with_message_code "Error: Unable to extract the bundle from ${TEMP_DIR}/${VENV_PKG_FILE}!" 6
            fi
        else
            cp -r "${VENV_INST_DIR}" "${VENV_TMP_DIR}"
            pushd "${VENV_TMP_DIR}" > /dev/null
        fi
        grep -m1 -r "^#\!${VENV_INST_DIR}" bin/ | sed 's/:.*//' | sort | uniq | xargs -I '{}' sed -i "1s=^#!${VENV_INST_DIR}/bin/.*=#!${VENV_TMP_DIR}/bin/python=" {}
        sed -i "s#${VENV_INST_DIR}#${VENV_TMP_DIR}#g" bin/python
        popd > /dev/null
        echo "${VENV_HASH}" > "${VENV_TMP_DIR}/${VENV_HASH_FILE}"
    fi
else
    if [ ! -f "${VENV_TMP_DIR}/${VENV_HASH_FILE}" ]; then
        exit_with_message_code "Error: Unable to download ${CLIENT_REPO_URL}/${VENV_FILE} file!" 3
    fi
fi
