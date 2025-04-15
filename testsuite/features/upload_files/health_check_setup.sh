#!/bin/bash

HEALTH_CHECK_PACKAGE="python311-mgr-health-check"
HEALTH_CHECK_REPO="https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/healthcheck/15.6/"
HEALTH_CHECK_CONFIG="/usr/lib/python3.11/site-packages/health_check/config.toml"

set -e

if [ "$1" == "setup" ]; then
        echo "PREPARING HEALTH CHECK TOOL ENVIRONMENT"
        echo
	echo "---> Configuring OBS health check repository ($HEALTH_CHECK_REPO)"
        zypper ar $HEALTH_CHECK_REPO health_check_master_repo
        echo "---> Installing $HEALTH_CHECK_PACKAGE package"
	zypper in -y $HEALTH_CHECK_PACKAGE
        echo "---> Using devel health check containers instead of stable ones"
        sed -i 's/healthcheck\/stable/healthcheck/g' $HEALTH_CHECK_CONFIG
        echo "---> Display health check config.toml"
        cat $HEALTH_CHECK_CONFIG
        echo "---> Done!"
fi

if [ "$1" == "clean" ]; then
	echo "Removing health check tool and disable repository"
        zypper rr $HEALTH_CHECK_REPO
	zypper rm -y $HEALTH_CHECK_PACKAGE
fi
