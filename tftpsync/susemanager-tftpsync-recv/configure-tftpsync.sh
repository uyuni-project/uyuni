#!/bin/sh

# determine PROXY network configuration
SUMA_PROXY_FQDN=$( hostname -f )
if [ -z "$SUMA_PROXY_FQDN" ]; then
	echo "Could not determine hostname of this machine! Tried: hostname  -f" >&2;
	echo "Rerun this script once you solved the issue.";
	exit 1;
fi;

if ! host $SUMA_PROXY_FQDN &> /dev/null; then
	echo "Could not determine IP of $SUMA_PROXY_FQDN ! Tried: host $SUMA_PROXY_FQDN" >&2;
	echo "Rerun this script once you solved the issue.";
	exit 1;
fi;

SUMA_PROXY_IP=$( host $SUMA_PROXY_FQDN | awk '{ print $4 }' || echo "" )


# determine SUMA network configuration
SUMA_FQDN=$( egrep -m1 "^proxy.rhn_parent[[:space:]]*=" /etc/rhn/rhn.conf | sed 's/^proxy.rhn_parent[[:space:]]*=[[:space:]]*\(.*\)/\1/' || echo "" )
if [ -z "$SUMA_FQDN" ]; then
	echo "Could not determine SUMA Hostname! Got /etc/rhn/rhn.conf vanished?" >&2;
	echo "Rerun this script once you solved the issue.";
	exit 1;
fi;

if ! host $SUMA_FQDN &> /dev/null; then
	echo "Could not determine IP of $SUMA_FQDN ! Tried: host $SUMA_FQDN" >&2;
	echo "Rerun this script once you solved the issue.";
	exit 1;
fi;

SUMA_IP=$(  host $SUMA_FQDN | awk '{ print $4 }' )


cat << EOF
Using following network configuration for this SUSE Manager Proxy:
------------------------------------------------------------------

 * SUSE Manager PROXY FQDN = $SUMA_PROXY_FQDN
 * SUSE Manager PROXY IP = $SUMA_PROXY_IP

 * SUSE Manager FQDN = $SUMA_FQDN
 * SUSE Manager IP = $SUMA_IP

If any of this settings is wrong you need to correct following files:

 * /etc/apache2/conf.d/zz-susemanager-proxy-distributed-cobbler.conf
 * /etc/rhn/rhn.conf
EOF

## Patch network configuration #######
######################################

if ! egrep -m1 "^proxy.rhn_parent_ip[[:space:]]*=" /etc/rhn/rhn.conf >/dev/null; then
    echo "proxy.rhn_parent_ip = $SUMA_IP" >> /etc/rhn/rhn.conf
else
    sed -i 's/^proxy.rhn_parent_ip[[:space:]]*=.*/proxy.rhn_parent_ip = $SUMA_IP/' /etc/rhn/rhn.conf
fi
if ! egrep -m1 "^proxy.proxy_ip[[:space:]]*=" /etc/rhn/rhn.conf >/dev/null; then
    echo "proxy.proxy_ip = $SUMA_PROXY_IP" >> /etc/rhn/rhn.conf
else
    sed -i 's/^proxy.proxy_ip[[:space:]]*=.*/proxy.proxy_ip = $SUMA_PROXY_IP/' /etc/rhn/rhn.conf
fi
if ! egrep "^proxy.proxy[[:space:]]*=" /etc/rhn/rhn.conf >/dev/null; then
    echo "proxy.proxy = $SUMA_PROXY_FQDN" >> /etc/rhn/rhn.conf
else
    sed -i 's/^proxy.proxy[[:space:]]*=.*/proxy.proxy = $SUMA_PROXY_FQDN/' /etc/rhn/rhn.conf
fi

for n in \
	/etc/apache2/conf.d/zz-susemanager-proxy-distributed-cobbler.conf; do

	sed -i "s/@@SUMA_PROXY_FQDN@@/$SUMA_PROXY_FQDN/g" $n
	sed -i "s/@@SUMA_FQDN@@/$SUMA_FQDN/g" $n

	sed -i "s/@@SUMA_PROXY_IP@@/$SUMA_PROXY_IP/g" $n
	sed -i "s/@@SUMA_IP@@/$SUMA_IP/g" $n
done


#######################################

if [ ! -d "/srv/tftpboot" ]; then
    mkdir /srv/tftpboot
fi
chown wwwrun.www /srv/tftpboot
cp /etc/sysconfig/atftpd /etc/sysconfig/atftpd.orig
sed -i 's%^ATFTPD_DIRECTORY="\(.*\)"%ATFTPD_DIRECTORY="/srv/tftpboot"%' /etc/sysconfig/atftpd
chkconfig atftpd on
/etc/init.d/atftpd start


#cp /etc/apache2/conf.d/cobbler-proxy.conf /etc/apache2/conf.d/cobbler-proxy.conf.orig
#sed "s/\(RewriteRule\)/#\1/" /etc/apache2/conf.d/cobbler-proxy.conf

/etc/init.d/apache2 restart
