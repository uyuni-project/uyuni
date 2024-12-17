#!/bin/sh
# SPDX-License-Identifier: Apache-2.0
# SPDX-FileCopyrightText: (c) 2024 SUSE LLC

set -euo pipefail

test -f /.kconfig && . /.kconfig
test -f /.profile && . /.profile

echo "Configure image: [$kiwi_iname]..."

systemctl enable prometheus-node_exporter
systemctl enable timezone_alignment
systemctl enable sssd


# Initialize environments to sync configuration and package files to persistent volumes
uyuni-configfiles-sync init /etc/apache2/
uyuni-configfiles-sync init /etc/cobbler/
uyuni-configfiles-sync init /etc/postfix/
uyuni-configfiles-sync init /etc/rhn/
uyuni-configfiles-sync init /etc/salt/
uyuni-configfiles-sync init /etc/sysconfig/
uyuni-configfiles-sync init /etc/tomcat/
uyuni-configfiles-sync init /srv/tftpboot/
uyuni-configfiles-sync init /srv/www/
uyuni-configfiles-sync init /var/lib/cobbler/

# Clean dangling gpg-agent socket file
rm root/.gnupg/S.gpg-agent

#=======================================
# Clean up after zypper if it is present
#---------------------------------------
if command -v zypper > /dev/null; then
    zypper -n clean
fi

rm -rf /var/log/zypp

exit 0
