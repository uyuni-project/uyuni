#!/bin/bash

set -xe

if [ -n "$1" ]; then
  # update
  zypper ar -G http://download.opensuse.org/update/leap/15.4/sle/ sle_update_repo
  zypper ar -G http://download.opensuse.org/update/leap/15.4/oss/ os_update_repo
  zypper ar -G http://download.opensuse.org/update/leap/15.4/backports/ backports_update_repo

  # distribution
  zypper ar -G http://download.opensuse.org/distribution/leap/15.4/repo/oss/ os_pool_repo

  # product
  zypper ar -G http://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master/images/repo/Uyuni-Server-POOL-x86_64-Media1/ server_pool_repo
  zypper ar -G http://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master/images/repo/Testing-Overlay-POOL-x86_64-Media1/ testing_overlay_devel_repo
fi
