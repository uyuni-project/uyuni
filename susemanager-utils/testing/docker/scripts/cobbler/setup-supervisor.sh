#!/bin/bash

echo "Copy supervisord confiuration file"
cp -r /root/cobbler/supervisord/conf.d/* /etc/supervisord.d/
cp /root/cobbler/supervisord/supervisord.conf /etc/

echo "Setup openLDAP"
/root/cobbler/setup-openldap.sh

echo "Setup reposync"
/root/cobbler/setup-reposync.sh

echo "Setup MongoDB"
/root/cobbler/setup-mongodb.sh

echo "Load supervisord configuration file and wait 5s"
supervisord -c /etc/supervisord.conf
sleep 5

echo "Load openLDAP database"
ldapadd -Y EXTERNAL -H ldapi:/// -f /root/cobbler/test.ldif

echo "Create DHCPD leases file"
touch /var/lib/dhcp/db/dhcpd.leases

echo "Show Cobbler version"
cobbler version
