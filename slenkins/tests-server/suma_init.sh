#!/bin/bash -ex 
sut_dir="/var/lib/slenkins/tests-suse-manager/tests-server"

mv $sut_dir/*.rpm /root/

zypper ar http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/3.0/images/repo/SUSE-Manager-Server-3.0-POOL-x86_64-Media1/ suma3
#zypper ar http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/Head/images/repo/SLE-12-Manager-Tools-POOL-x86_64-Media1/ suma3_tools

zypper -n --gpg-auto-import-keys ref
# here we should need the source packages.
zypper -n in subscription-tools
zypper -n in spacewalk-client-setup
zypper -n in spacewalk-check
zypper -n in rhncfg-actions

zypper -n in -t pattern suma_server
zypper -n in timezone
echo "+++++++++++++++++++++++"
echo "installing packages ok"
echo "+++++++++++++++++++++++"
#
echo 'MANAGER_USER="spacewalk"
MANAGER_PASS="spacewalk"
MANAGER_ADMIN_EMAIL="galaxy-noise@suse.de"
CERT_O="Novell"
CERT_OU="SUSE"
CERT_CITY="Nuernberg"
CERT_STATE="Bayern"
CERT_COUNTRY="DE"
CERT_EMAIL="galaxy-noise@suse.de"
CERT_PASS="spacewalk"
MANAGER_DB_NAME="susemanager"
MANAGER_DB_HOST="localhost"
MANAGER_DB_PORT="5432"
MANAGER_DB_PROTOCOL="TCP"
MANAGER_ENABLE_TFTP="Y"
SCC_USER="UC7"
SCC_PASS="a48210ea39"
' > /root/setup_env.sh

bash -x /usr/lib/susemanager/bin/migration.sh -l /var/log/susemanager_setup.log -s 
if [ $? -ne 0]; then exit 1; fi
cat  /var/log/susemanager_setup.log
echo -e "\nserver.susemanager.mirror = http://smt-scc.nue.suse.com" >> /etc/rhn/rhn.conf
sed -i "s/^server.satellite.no_proxy[[:space:]]*=.*/server.satellite.no_proxy = smt-scc.nue.suse.com/" /etc/rhn/rhn.conf
