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

NEW_VERSION_ID=15.5

CHECK_OS=$(cat /etc/os-release  | grep PRETTY_NAME)

if [[ ${CHECK_OS} != *"openSUSE Leap"* ]]; then
  echo "Please check your OS. openSUSE Leap is needed "
  exit -1
fi

spacewalk-service stop
rm -rf /etc/zypp/repos.d.old
mv /etc/zypp/repos.d /etc/zypp/repos.d.old
mkdir /etc/zypp/repos.d
zypper ar -n "Main Repository" http://download.opensuse.org/distribution/leap/${NEW_VERSION_ID}/repo/oss repo-oss
zypper ar -n "Main Update Repository" http://download.opensuse.org/update/leap/${NEW_VERSION_ID}/oss repo-update
zypper ar -n "Non-OSS Repository" http://download.opensuse.org/distribution/leap/${NEW_VERSION_ID}/repo/non-oss repo-non-oss
zypper ar -n "Update Repository (Non-Oss)" http://download.opensuse.org/update/leap/${NEW_VERSION_ID}/non-oss/ repo-update-non-oss
zypper ar -n "Uyuni Server Stable" https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable/images/repo/Uyuni-Server-POOL-x86_64-Media1/ server-stable
zypper ar -n "Update repository with updates from SUSE Linux Enterprise" http://download.opensuse.org/update/leap/${NEW_VERSION_ID}/sle repo-sle-update
zypper ar -n "Update repository of openSUSE Backports" http://download.opensuse.org/update/leap/${NEW_VERSION_ID}/backports/ repo-backports-update
zypper ref
zypper -n dup --allow-vendor-change
ret=${?}
if [[ ${ret} -ne 0 ]];then
    echo "Migration went wrong. Please fix the issues and try again. return code is ${ret}"
    exit -1
fi

CURRENT_VERSION_ID=$(cat /etc/os-release  | grep VERSION_ID | cut -f2 -d=)

if [[ "${CURRENT_VERSION_ID}" != "\"${NEW_VERSION_ID}\"" ]]; then
    echo "Migration went wrong. Expected version is ${NEW_VERSION_ID} instead is ${CURRENT_VERSION_ID}"
    echo "Please try to re-run this script"
    exit -1
fi

echo "==================================================================="
echo "OS migrated successfully"
#TODO Upgrade from 15.4 to 15.5 will not migrate postgres
#echo "Now please migrate to the new postgres version "
#echo "by running /usr/lib/susemanager/bin/pg-migrate-x-to-y.sh"
echo
echo "Reboot system afterwards."
echo "==================================================================="
