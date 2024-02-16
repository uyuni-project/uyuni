#!/usr/bin/bash

# SPDX-FileCopyrightText: 2024 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

# This script is called by push-packages-to-obs
# To use it add the following to the tito.props of the package:
#
# [buildconfig]
# builder = custom.ContainerBuilder

OSCAPI=$1
GIT_DIR=$2
PKG_NAME=$3

SRPM_PKG_DIR=$(dirname "$0")

# check which endpoint we are using to match the product
if [ "${OSCAPI}" == "https://api.suse.de" ]; then
  PRODUCT_VERSION="$(sed -n 's/.*web.version\s*=\s*\(.*\)$/\1/p' ${GIT_DIR}/web/conf/rhn_web.conf)"
else
  # Uyuni settings
  PRODUCT_VERSION="$(sed -n 's/.*web.version.uyuni\s*=\s*\(.*\)$/\1/p' ${GIT_DIR}/web/conf/rhn_web.conf)"
fi
# to lowercase with ",," and replace spaces " " with "-"
PRODUCT_VERSION=$(echo ${PRODUCT_VERSION,,} | sed -r 's/ /-/g')


if [ -f "${SRPM_PKG_DIR}/Dockerfile" ]; then
  NAME="${PKG_NAME%%-image}"
  # check which endpoint we are using to match the product
  if [ "${OSCAPI}" == "https://api.suse.de" ]; then
    # SUSE Manager settings
    VERSION=$(echo ${PRODUCT_VERSION} | sed 's/^\([0-9]\+\.[0-9]\+\).*$/\1/')
    sed "/^#\!BuildTag:/s/uyuni/suse\/manager\/${VERSION}/g" -i ${SRPM_PKG_DIR}/Dockerfile
    sed "s/^\(LABEL org.opensuse.reference=\)\"\([^:]\+:\)\([^%]\+\)%RELEASE%\"/\1\"\2${PRODUCT_VERSION}.%RELEASE%\"/" -i ${SRPM_PKG_DIR}/Dockerfile
    sed "/^# labelprefix=/s/org\.opensuse\.uyuni/com.suse.manager/" -i ${SRPM_PKG_DIR}/Dockerfile
    sed "s/^ARG VENDOR=.*$/ARG VENDOR=\"SUSE LLC\"/" -i ${SRPM_PKG_DIR}/Dockerfile
    sed "s/^ARG PRODUCT=.*$/ARG PRODUCT=\"SUSE Manager\"/" -i ${SRPM_PKG_DIR}/Dockerfile
    sed "s/^ARG URL=.*$/ARG URL=\"https:\/\/www.suse.com\/products\/suse-manager\/\"/" -i $SRPM_PKG_DIR/Dockerfile
    sed "s/^ARG REFERENCE_PREFIX=.*$/ARG REFERENCE_PREFIX=\"registry.suse.com\/suse\/manager\/${VERSION}\"/" -i $SRPM_PKG_DIR/Dockerfile
    sed "/^# labelprefix=.*$/aLABEL com.suse.eula=\"sle-eula\"" -i ${SRPM_PKG_DIR}/Dockerfile
    sed "/^# labelprefix=.*$/aLABEL com.suse.release-stage=\"released\"" -i ${SRPM_PKG_DIR}/Dockerfile
    sed "/^# labelprefix=.*$/aLABEL com.suse.lifecycle-url=\"https://www.suse.com/lifecycle/\"" -i ${SRPM_PKG_DIR}/Dockerfile
    sed "/^# labelprefix=.*$/aLABEL com.suse.supportlevel=\"l3\"" -i ${SRPM_PKG_DIR}/Dockerfile
    NAME="suse\/manager\/${VERSION}\/${NAME}"
  else
    NAME="uyuni\/${NAME}"
  fi

  sed "/^ARG REFERENCE_PREFIX=.*$/aARG PRODUCT_VERSION=\"${PRODUCT_VERSION}\"" -i ${SRPM_PKG_DIR}/Dockerfile
  # Add version from rhn_web on top of version from tito to have a continuity with already relased versions
  sed "/^#\!BuildTag:/s/$/ ${NAME}:${PRODUCT_VERSION} ${NAME}:${PRODUCT_VERSION}.%RELEASE%/" -i ${SRPM_PKG_DIR}/Dockerfile

  rm ${SRPM_PKG_DIR}/${PKG_NAME}.spec
fi

