# $Id: default.cfg,v 1.9 2005/11/06 06:38:05 jmates Exp $
#
# Example KickStart configuration for Fedora systems.
#
# For more documentation, see: http://sial.org/howto/kickstart/

install
$my_var
text

nfs --server 192.0.2.1 --dir /install/fedora/2/i386/os

# RedHat likes to set UTF8, which under RH 9 causes various problems...
lang en_US
langsupport --default en_US en_US

# run system-config-* as needed once in production
keyboard us
mouse none
skipx

# root password is 'linux'
network --device eth0 --bootproto dhcp
rootpw --iscrypted $6$LdF0Ddaf5q/wqYk6$x04erFOijr7U82EB2GL24Ko4yWvyVo4S91bg9Yp08PLDcLBwmxwJpfKox1vlZ/faFED.dbfAe5ofgoJtHCkia.
authconfig --enableshadow --enablemd5

firewall --enabled --ssh

timezone --utc US/Pacific

# partition done via script, below
part swap --recommended
part / --fstype ext3 --size 1 --grow


# packages done via script, below
%include /tmp/base-packages

%pre

# load specific boot args into variables
< /proc/cmdline sed 's/ /\n/g' | grep ^cf_ | grep = > /tmp/cf-args
. /tmp/cf-args

export RH_MOUNT=/tmp-build
mkdir -p $RH_MOUNT
mount -t nfs 192.0.2.1:/install $RH_MOUNT

# custom package list by name, otherwise interactive
touch /tmp/base-packages
if [ -f "$RH_MOUNT/kickstart/fedora/packages/$cf_pkg" ]; then
  cp "$RH_MOUNT/kickstart/fedora/packages/$cf_pkg" /tmp/base-packages
fi

%post


