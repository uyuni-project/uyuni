#!/bin/bash

set -xe

# HACK: avoid adding repos if building inside of OBS (repos come from project configuration)
if [ -n "$1" ]; then
    echo "█████████████████████████████████████████████████████████████████████████████████"
    echo "█ WARNING!!! using Leap 15.4 repositories! See the add_repos.sh for more detail █"
    echo "█████████████████████████████████████████████████████████████████████████████████"
    zypper addrepo http://download.opensuse.org/distribution/leap/15.4/repo/oss/ main
    zypper addrepo http://download.opensuse.org/update/leap/15.4/sle/ updates
    zypper addrepo http://download.opensuse.org/update/leap/15.4/oss/ updates-oss

    zypper addrepo $1 product
fi
