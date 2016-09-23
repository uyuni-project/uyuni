#!/bin/bash -ex 
sut_dir="/var/lib/slenkins/tests-suse-manager/tests-server"

mv $sut_dir/*.rpm /root/
# gmc 
# zypper ar http://dist.suse.de/install/SLP/SUSE-Manager-Server-3-GM/x86_64/DVD1/ suma3-gmc

# devel HEAD
#zypper ar http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/Head/images/repo/SUSE-Manager-Server-3.0-POOL-x86_64-Media1/ suma3_devel_head

zypper ar http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/3.0/images/repo/SUSE-Manager-Server-3.0-POOL-x86_64-Media1/ suma3_devel

zypper -n --gpg-auto-import-keys ref
zypper -n in --auto-agree-with-licenses -t pattern suma_server
zypper -n in timezone
# tools devel
# zypper ar http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/Head/images/repo/SLE-12-Manager-Tools-POOL-x86_64-Media1/ suma3_tools
# zypper ar http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/Head/images/repo/SLE-12-Manager-Tools-POOL-x86_64-Media1/ suma3_tools

## updates are here :
#zypper ar http://download.suse.de/ibs/SUSE/Updates/SUSE-Manager-Server/3.0/x86_64/update/SUSE:Updates:SUSE-Manager-Server:3.0:x86_64.repo
#zypper -n up -r  SUSE_Updates_SUSE-Manager-Server_3.0_x86_64 -l
#zypper -n up -r suma3_tools -l 


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

bash -x /usr/lib/susemanager/bin/migration.sh -l /var/log/susemanager_setup.log -s 
if [ $? -ne 0]; then exit 1; fi
cat  /var/log/susemanager_setup.log
echo -e "\nserver.susemanager.mirror = http://smt-scc.nue.suse.com" >> /etc/rhn/rhn.conf
sed -i "s/^server.satellite.no_proxy[[:space:]]*=.*/server.satellite.no_proxy = smt-scc.nue.suse.com/" /etc/rhn/rhn.conf
