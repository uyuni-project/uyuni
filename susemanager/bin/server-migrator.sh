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
cp -a /var/lib/cobbler /var/lib/cobbler.old
cp -a /var/lib/rhn/kickstarts /var/lib/rhn/kickstarts.old
rm /var/lib/cobbler/snippets/spacewalk
mv /etc/zypp/repos.d /etc/zypp/repos.d.old
mkdir /etc/zypp/repos.d
zypper ar -n "Main Repository" http://download.opensuse.org/distribution/leap/15.1/repo/oss repo-oss
zypper ar -n "Main Update Repository" http://download.opensuse.org/update/leap/15.1/oss repo-update
zypper ar -n "Non-OSS Repository" http://download.opensuse.org/distribution/leap/15.1/repo/non-oss repo-non-oss
zypper ar -n "Update Repository (Non-Oss)" http://download.opensuse.org/update/leap/15.1/non-oss/ repo-update-non-oss
zypper ar -n "Uyuni Server Stable" https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable/images/repo/Uyuni-Server-4.0-POOL-x86_64-Media1/ uyuni-server-stable
zypper ref
rpm -e --nodeps atftp
rpm -e --nodeps python-Cheetah
rpm -e --nodeps apache2-mod_wsgi
rpm -e --nodeps python-netaddr
rpm -e --nodeps pxe-default-image-opensuse42-3
zypper -n dup
update-alternatives --set java /usr/lib64/jvm/jre-11-openjdk/bin/java
update-alternatives --set servlet /usr/share/java/servletapi5-5.0.18.jar
spacewalk-schema-upgrade -y
if [ -d /var/lib/cobbler.old/config ]; then
  cp -a /var/lib/cobbler.old/config /var/lib/cobbler
fi
if [ -d /var/lib/rhn/kickstarts.old/upload ]; then
  cp -a /var/lib/rhn/kickstarts.old/upload /var/lib/rhn/kickstarts
fi
if [ -d /var/lib/rhn/kickstarts.old/wizard ]; then
  cp -a /var/lib/rhn/kickstarts.old/wizard /var/lib/rhn/kickstarts
fi
rm -rf /var/lib/cobbler.old /var/lib/rhn/kickstarts.old
/usr/lib/susemanager/bin/migrate-cobbler.sh

echo
echo "==================================================================="
echo "Reboot the machine for the new version now!"
echo "==================================================================="
