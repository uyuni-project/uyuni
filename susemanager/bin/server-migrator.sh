#!/bin/bash

echo
echo
echo "==================================================================="
echo "This script will migrate Uyuni server to the latest version which"
echo "also implies replacing the underlying operating system."
echo
echo "During migration the services need to be shut down and after"
echo "successful migration the server needs to be rebooted manually."
echo
echo "Since there is no chance to fix any issues during migration,"
echo "make sure you have a backup before continuing. If you are"
echo "running Uyuni server on a virtual machine, it is advisable"
echo "to create a snapshot before performing the migration!"
echo "==================================================================="
echo
echo
read -n 1 -s -r -p "Press any key to start the migration or CTRL+C to cancel...";
echo

spacewalk-service stop
mv /etc/zypp/repos.d /etc/zypp/repos.d.old
mkdir /etc/zypp/repos.d
zypper ar -n "Main Repository" http://download.opensuse.org/distribution/leap/15.3/repo/oss repo-oss
zypper ar -n "Main Update Repository" http://download.opensuse.org/update/leap/15.3/oss repo-update
zypper ar -n "Non-OSS Repository" http://download.opensuse.org/distribution/leap/15.3/repo/non-oss repo-non-oss
zypper ar -n "Update Repository (Non-Oss)" http://download.opensuse.org/update/leap/15.3/non-oss/ repo-update-non-oss
zypper ar -n "Uyuni Server Stable" https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable/images/repo/Uyuni-Server-POOL-x86_64-Media1/ uyuni-server-stable
zypper ar -n "Update repository wiht updates from SUSE Linux Enterprise 15" http://download.opensuse.org/update/leap/15.3/sle repo-sle-update
zypper ref
zypper -n dup --allow-vendor-change

echo
echo "==================================================================="
echo "If you did not yet migrate the database to postgresql13, do so now"
echo "by running /usr/lib/susemanager/bin/pg-migrate-12-to-13.sh"
echo
echo "Reboot system afterwards."
echo "==================================================================="
