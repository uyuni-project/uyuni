#!/bin/bash -x 

#grep name packages.xml | cut -d'"' -f2
zypper ar http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/3.0/images/repo/SUSE-Manager-Server-3.0-POOL-x86_64-Media1/ suma3
zypper ar http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/Head/SLE_13_SP1/ devel_head
zypper ar http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/Head/images/repo/SLE-12-Manager-Tools-POOL-x86_64-Media1/ suma3_tools

zypper -n --gpg-auto-import-keys ref
zypper -n in susemanager smdba spacewalk-postgresql spacewalk-reports cyrus-sasl-digestmd5

zypper -n in perl-TimeDate susemanager-tftpsync salt-master salt-api python-ws4py
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
SCC_USER="UCUSER"
SCC_PASS="UCPASSWORD"
' > /root/setup_env.sh

/usr/lib/susemanager/bin/migration.sh -l /var/log/susemanager_setup.log -s 

cat  /var/log/susemanager_setup.log
#echo -e "\nserver.susemanager.mirror = http://smt-scc.nue.suse.com" >> /etc/rhn/rhn.conf
#sed -i "s/^server.satellite.no_proxy[[:space:]]*=.*/server.satellite.no_proxy = smt-scc.nue.suse.com/" /etc/rhn/rhn.conf
