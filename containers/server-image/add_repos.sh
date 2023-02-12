#!/bin/bash

# update
zypper ar -f -G http://download.opensuse.org/update/leap/15.4/sle/ sle_update_repo
zypper ar -f -G http://download.opensuse.org/update/leap/15.4/oss/ os_update_repo
zypper ar -f -G http://download.opensuse.org/update/leap/15.4/backports/ backports_update_repo

# distribution
zypper ar -f -G http://download.opensuse.org/distribution/leap/15.4/repo/oss/ os_pool_repo

# product
zypper ar -G http://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master/images/repo/Testing-Overlay-POOL-x86_64-Media1/ testing_overlay_devel_repo
#TODO this is the repo that currently contains uyuni-server-systemd-services. Remove when changes are in master branch
zypper ar -f -G https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master:/ServerContainer/openSUSE_Leap_15.4/ systemsmanagement_Uyuni_Master_ServerContainer
#TODO uncomment when changes are in master branch
#zypper ar -f -G http://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master/images/repo/Uyuni-Server-POOL-x86_64-Media1/ server_pool_repo
