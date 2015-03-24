#! /bin/bash

if [ "$1" = "-h" -o "$1" = "--help" ]; then
    echo "susemanager-update-swcert.sh - update the spacewalk public certificate in the database"
    exit 0
fi

if [ $UID -ne 0 ]; then
    echo "You must run this as root."
    exit 1;
fi

LOGFILE="/var/log/susemanager-update-swcert.log"
# set -x
exec > >(tee -a $LOGFILE) 2>&1

echo "################################################"
date
echo "################################################"

echo "select TO_CHAR(expires, 'YYYY-MM-DD HH24:MI:SS') expires,
       expires - current_timestamp
          from rhnSatelliteCert
         where label = 'rhn-satellite-cert'
         and version = (select max(version) from rhnSatelliteCert);
" | spacewalk-sql --select-mode - | grep "2018-07-13" >/dev/null

if [ $? -eq 0 ]; then
    echo "Your spacewalk certificate is up-to-date"
    exit 0
fi

rhn-satellite-activate --disconnected --rhn-cert /usr/share/spacewalk/setup/spacewalk-public.cert

if [ $? -ne 0 ]; then
    echo
    echo "########################"
    echo "#### Update failed. ####"
    echo "########################"

    exit 1;
fi

if [ -e "/var/lib/spacewalk/scc/migrated" ]; then
    mgr-sync refresh
else
    mgr-ncc-sync --refresh
fi

echo
echo "#######################################"
echo "#### Update successfully finished. ####"
echo "#######################################"

