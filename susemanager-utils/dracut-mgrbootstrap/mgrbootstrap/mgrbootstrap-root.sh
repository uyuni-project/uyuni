#!/bin/sh

root=saltboot
rootok=1

# If we don't have a server, we need dhcp
if [ -z "$server" ] ; then
    DHCPORSERVER="1"
fi;

export DISABLE_UNIQUE_SUFFIX=$(getarg DISABLE_UNIQUE_SUFFIX=)
export kiwidebug=$(getarg kiwidebug=)
export spacewalk_activationkey=$(getarg spacewalk_activationkey=)
export spacewalk_hostname=$(getarg spacewalk_hostname=)
