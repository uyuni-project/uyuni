#!/bin/bash

# SPDX-FileCopyrightText: 2019-2026 SUSE LLC
#
# SPDX-License-Identifier: MIT
test -f /.kconfig && . /.kconfig
test -f /.profile && . /.profile

mkdir /var/lib/misc/reconfig_system

#======================================
# Greeting...
#--------------------------------------
echo "Configure image: [$name]..."

#======================================
# add missing fonts
#--------------------------------------
CONSOLE_FONT="eurlatgr.psfu"

#======================================
# prepare for setting root pw, timezone
#--------------------------------------
echo ** "reset machine settings"

# FIXME:
#sed -i 's/^root:[^:]*:/root:*:/' /etc/shadow
rm -f /etc/machine-id \
      /var/lib/zypp/AnonymousUniqueId \
      /var/lib/systemd/random-seed \
      /var/lib/dbus/machine-id

#======================================
# Setup baseproduct link
#--------------------------------------
suseSetupProduct

#======================================
# Specify default runlevel
#--------------------------------------
baseSetRunlevel 3

#======================================
# Add missing gpg keys to rpm
#--------------------------------------
suseImportBuildKey

#======================================
# Enable DHCP on eth0
#--------------------------------------
cat >/etc/sysconfig/network/ifcfg-eth0 <<EOF
BOOTPROTO='dhcp'
MTU=''
REMOTE_IPADDR=''
STARTMODE='auto'
ETHTOOL_OPTIONS=''
USERCONTROL='no'
EOF

#======================================
# Enable sshd
#--------------------------------------
chkconfig sshd on

#======================================
# Sysconfig Update
#--------------------------------------
echo '** Update sysconfig entries...'

baseUpdateSysConfig /etc/sysconfig/network/dhcp DHCLIENT_SET_HOSTNAME yes

# Enable firewalld
chkconfig firewalld on

# Set GRUB2 to boot graphically (bsc#1097428)
sed -Ei"" "s/#?GRUB_TERMINAL=.+$/GRUB_TERMINAL=gfxterm/g" /etc/default/grub
sed -Ei"" "s/#?GRUB_GFXMODE=.+$/GRUB_GFXMODE=auto/g" /etc/default/grub

# Systemd controls the console font now
echo FONT="$CONSOLE_FONT" >> /etc/vconsole.conf

#======================================
# SSL Certificates Configuration
#--------------------------------------
echo '** Rehashing SSL Certificates...'
update-ca-certificates

if [ ! -s /var/log/zypper.log ]; then
	> /var/log/zypper.log
fi

exit 0
