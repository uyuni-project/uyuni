#!/usr/bin/bash

# SPDX-FileCopyrightText: 2024 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

# This script is called by push-packages-to-obs
# To use it add the following to the tito.props of the package:
#
# [buildconfig]
# builder = custom.ContainerBuilder

PRODUCT=$1
GIT_DIR=$2
PKG_NAME=$3

SRPM_PKG_DIR=$(dirname "$0")

# convert legacy value
test "${PRODUCT}" == "https://api.suse.de" && PRODUCT="MLM"

# check which endpoint we are using to match the product
if [ "${PRODUCT}" == "MLM" ]; then
  PRODUCT_VERSION="$(sed -n 's/.*web.version\s*=\s*\(.*\)$/\1/p' ${GIT_DIR}/web/conf/rhn_web.conf)"
else
  # Uyuni settings
  PRODUCT_VERSION="$(sed -n 's/.*web.version.uyuni\s*=\s*\(.*\)$/\1/p' ${GIT_DIR}/web/conf/rhn_web.conf)"
fi
# to lowercase with ",," and replace spaces " " with "-"
PRODUCT_VERSION=$(echo ${PRODUCT_VERSION,,} | sed -r 's/ /-/g')

# Possible values: beta, sle-eula
EULA=beta

# Possible values: alpha, beta, released
RELEASE_STAGE=beta

for DOCKERFILE in $(ls ${SRPM_PKG_DIR}/Dockerfile* 2>/dev/null); do
  NAME="${PKG_NAME%%-image}"
  # check which endpoint we are using to match the product
  if [ "${PRODUCT}" == "MLM" ]; then
    # SUSE Multi-Linux Manager settings
    VERSION=$(echo ${PRODUCT_VERSION} | sed 's/^\([0-9]\+\.[0-9]\+\).*$/\1/')
    ARCH=
    # OBS cannot find the dependencies with a placeholder or %_arch from the project config.
    # The best solution is to only use the architecture in the path of the images to release,
    # not intermediary ones.
    if [ "${NAME}" != "init" ]; then
        ARCH="\/%ARCH%"
    fi
    sed "/^#\!BuildTag:/s/uyuni/suse\/multi-linux-manager\/${VERSION}${ARCH}/g" -i ${DOCKERFILE}
    sed "s/^\(LABEL org.opensuse.reference=\)\"\([^:]\+:\)\([^%]\+\)%RELEASE%\"/\1\"\2${PRODUCT_VERSION}.%RELEASE%\"/" -i ${DOCKERFILE}
    sed "/^# labelprefix=/s/org\.opensuse\.uyuni/com.suse.multilinuxmanager/" -i ${DOCKERFILE}
    sed "s/^ARG VENDOR=.*$/ARG VENDOR=\"SUSE LLC\"/" -i ${DOCKERFILE}
    sed "s/^ARG PRODUCT=.*$/ARG PRODUCT=\"SUSE Multi-Linux Manager\"/" -i ${DOCKERFILE}
    sed "s/^LABEL org\.opensuse\.reference=\"\${REFERENCE_PREFIX}/LABEL org.opensuse.reference=\"\${REFERENCE_PREFIX}${ARCH}/" -i ${DOCKERFILE}
    sed "/^# labelprefix=.*$/aLABEL com.suse.eula=\"${EULA}\"" -i ${DOCKERFILE}
    sed "/^# labelprefix=.*$/aLABEL com.suse.release-stage=\"${RELEASE_STAGE}\"" -i ${DOCKERFILE}
    sed "/^# labelprefix=.*$/aLABEL com.suse.lifecycle-url=\"https://www.suse.com/lifecycle/\"" -i ${DOCKERFILE}
    sed "/^# labelprefix=.*$/aLABEL com.suse.supportlevel=\"l3\"" -i ${DOCKERFILE}
    NAME="suse\/multi-linux-manager\/${VERSION}${ARCH}\/${NAME}"
  else
    NAME="uyuni\/${NAME}"
  fi

  sed "/^ARG REFERENCE_PREFIX=.*$/aARG PRODUCT_VERSION=\"${PRODUCT_VERSION}\"" -i ${DOCKERFILE}
  # Add version from rhn_web on top of version from tito to have a continuity with already relased versions
  sed "/^#\!BuildTag:/s/BuildTag:/BuildTag: ${NAME}:${PRODUCT_VERSION} ${NAME}:${PRODUCT_VERSION}.%RELEASE%/" -i ${DOCKERFILE}

  rm -f ${SRPM_PKG_DIR}/${PKG_NAME}.spec
done

