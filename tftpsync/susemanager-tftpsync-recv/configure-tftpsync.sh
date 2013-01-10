#!/bin/sh
#
# Copyright (c) 2013 Novell, Inc
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#

if [ 0$UID -gt 0 ]; then
       echo Run as root.
       exit 1
fi

print_help () {
        cat <<HELP
usage: configure-tftpsync.sh [options]

options:
  -h, --help
            show this help message and exit
  --answer-file=filename
            Indicates the location of an answer file to be use for answering
            questions asked during the installation process. See man page for
            for an example and documentation.
  --non-interactive
            For use only with --answer-file. If the --answer-file doesn't
            provide a required response, default answer is used.
  --tftpbootdir=directory
            Path to tftp boot directory
  --parent-ip=ipaddress
            IP Address of the SUSE Manager Parent
  --proxy-fqdn=hostname
            FQDN of this SUSE Manager Proxy
  --proxy-ip=ipaddress
            IP Address of this SUSE Manager Proxy

HELP
        exit
}

parse_answer_file () {
    local FILE="$1"
    local ALIAS
    . $(echo $FILE | cut -d= -f2-)
    for ALIAS in ${SSL_CNAME[@]}; do
        SSL_CNAME_PARSED[CNAME_INDEX++]=--set-cname=$ALIAS
    done
}

INTERACTIVE=1

while [ $# -ge 1 ]; do
    case $1 in
        --help | -h)  print_help;;
        --answer-file=*) parse_answer_file $1;;
        --non-interactive) INTERACTIVE=0;;
        --tftpbootdir) TFTPBOOT=$(echo $1 | cut -d= -f2-);;
        --parent-ip) SUMA_IP=$(echo $1 | cut -d= -f2-);;
        --proxy-fqdn) SUMA_PROXY_FQDN=$(echo $1 | cut -d= -f2-);;
        --proxy-ip) SUMA_PROXY_IP=$(echo $1 | cut -d= -f2-);;
        *) echo Error: Invalid option $1
    esac
    shift
done

default_or_input () {
        local MSG="$1"
        local VARIABLE="$2"
        local DEFAULT="$3"

        local INPUT
        local CURRENT_VALUE=$(eval "echo \$$VARIABLE")
        #in following code is used not so common expansion
        #var_a=${var_b:-word}
        #which is like: var_a = $var_b ? word
        DEFAULT=${CURRENT_VALUE:-$DEFAULT}
        local VARIABLE_ISSET=$(set | grep "^$VARIABLE=")

        echo -n "$MSG [$DEFAULT]: "
        if [ "$INTERACTIVE" = "1" -a  -z "$VARIABLE_ISSET" ]; then
                read INPUT
        elif [ -z "$VARIABLE_ISSET" ]; then
                echo $DEFAULT
        else
                eval "DEFAULT=\$$VARIABLE"
                echo $DEFAULT
        fi
        if [ -z "$INPUT" ]; then
                INPUT="$DEFAULT"
        fi
        eval "$VARIABLE='$INPUT'"
}

SUMA_FQDN=$( egrep -m1 "^proxy.rhn_parent[[:space:]]*=" /etc/rhn/rhn.conf | sed 's/^proxy.rhn_parent[[:space:]]*=[[:space:]]*\(.*\)/\1/' || echo "" )
if [ -z "$SUMA_FQDN" ]; then
        echo "Could not determine SUSE Manager Parent Hostname! Got /etc/rhn/rhn.conf vanished?" >&2;
        echo "Rerun this script once you solved the issue.";
        exit 1;
fi;

default_or_input "TFTP Boot directory" TFTPBOOT $(egrep -m1 "^ATFTPD_DIRECTORY=" /etc/sysconfig/atftpd | sed 's/^ATFTPD_DIRECTORY="\(.*\)"[[:space:]]*$/\1/' || echo "")

default_or_input "SUSE Manager Proxy FQDN" SUMA_PROXY_FQDN $(hostname -f)

default_or_input "SUSE Manager Proxy IP Address" SUMA_PROXY_IP $(host $SUMA_PROXY_FQDN | awk '{ print $4 }' || echo "")

default_or_input "SUSE Manager Parent IP Address" SUMA_IP $(  host $SUMA_FQDN | awk '{ print $4 }' )



cat << EOF
Using the following configuration for this SUSE Manager Proxy:
--------------------------------------------------------------

 * TFTP Boot directory = $TFTPBOOT

 * SUSE Manager Proxy FQDN = $SUMA_PROXY_FQDN
 * SUSE Manager Proxy IP = $SUMA_PROXY_IP

 * SUSE Manager Parent FQDN = $SUMA_FQDN
 * SUSE Manager Parent IP = $SUMA_IP

If any of this settings are wrong please re-run this script!

EOF

## Patch network configuration #######
######################################

if ! egrep -m1 "^proxy.rhn_parent_ip[[:space:]]*=" /etc/rhn/rhn.conf >/dev/null; then
    echo "proxy.rhn_parent_ip = $SUMA_IP" >> /etc/rhn/rhn.conf
else
    sed -i "s/^proxy.rhn_parent_ip[[:space:]]*=.*/proxy.rhn_parent_ip = $SUMA_IP/" /etc/rhn/rhn.conf
fi
if ! egrep -m1 "^proxy.proxy_ip[[:space:]]*=" /etc/rhn/rhn.conf >/dev/null; then
    echo "proxy.proxy_ip = $SUMA_PROXY_IP" >> /etc/rhn/rhn.conf
else
    sed -i "s/^proxy.proxy_ip[[:space:]]*=.*/proxy.proxy_ip = $SUMA_PROXY_IP/" /etc/rhn/rhn.conf
fi
if ! egrep "^proxy.proxy[[:space:]]*=" /etc/rhn/rhn.conf >/dev/null; then
    echo "proxy.proxy = $SUMA_PROXY_FQDN" >> /etc/rhn/rhn.conf
else
    sed -i "s/^proxy.proxy[[:space:]]*=.*/proxy.proxy = $SUMA_PROXY_FQDN/" /etc/rhn/rhn.conf
fi
if ! egrep "^proxy.tftpboot[[:space:]]*=" /etc/rhn/rhn.conf >/dev/null; then
    echo "proxy.tftpboot = $TFTPBOOT" >> /etc/rhn/rhn.conf
else
    sed -i "s!^proxy.tftpboot[[:space:]]*=.*!proxy.tftpboot = $TFTPBOOT!" /etc/rhn/rhn.conf
fi


sed -i "s/^[[:space:]]*allow from[[:space:]].*$/    allow from $SUMA_IP/" /etc/apache2/conf.d/susemanager-tftpsync-recv.conf


#######################################

if [ ! -d "$TFTPBOOT" ]; then
    mkdir "$TFTPBOOT"
fi
chown wwwrun.www "$TFTPBOOT"
sed -i 's/^ATFTPD_DIRECTORY="\(.*\)"[[:space:]]*$/ATFTPD_DIRECTORY=""/' /etc/sysconfig/atftpd
sysconf_addword /etc/sysconfig/atftpd ATFTPD_DIRECTORY "$TFTPBOOT"
chkconfig atftpd on
/etc/init.d/atftpd start
/etc/init.d/apache2 restart
