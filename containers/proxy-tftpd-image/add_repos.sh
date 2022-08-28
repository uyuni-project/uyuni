#!/bin/bash

set -xe

# HACK: avoid adding repos if building inside of OBS (repos come from project configuration)
if [ -n "$1" ]; then
    zypper addrepo http://download.opensuse.org/distribution/leap/15.3/repo/oss/ main
    zypper addrepo http://download.opensuse.org/update/leap/15.3/sle/ updates
    zypper addrepo https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master:/Other/openSUSE_Leap_15.4/ uyuni-other

    zypper addrepo $1 product
fi
