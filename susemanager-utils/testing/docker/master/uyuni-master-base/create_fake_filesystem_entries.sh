#!/bin/bash
set -e

# spacewalk-setup requires the following binaries in this precise place (cheating with
# PATH does not help):
#
#   * /usr/sbin/spacewalk-service
#   * /usr/bin/rhn-config-schema.pl
#
# These binaries are alredy inside of the git checkout, we just need some
# symbolic links pointing to them. We do not have the git checkout mounted
# inside of the container at build time, hence we are going to cheat a little...
#
# The symlinks are broken right now, but they will work fine when we mount our
# git checkout on /manager


ln -s /manager/spacewalk/admin/spacewalk-service /usr/sbin/
ln -s /manager/spacewalk/admin/rhn-config-schema.pl /usr/bin/
ln -s /manager/spacewalk/admin/rhn-config-satellite.pl /usr/bin/
ln -s /manager/spacewalk/setup/bin/spacewalk-make-mount-points /usr/bin/
ln -s /manager/spacewalk/setup/bin/spacewalk-setup-sudoers /usr/bin/
mkdir -p /var/lib/rhn/rhn-satellite-prep/etc/rhn

mkdir -p /usr/share/rhn/config-defaults
ln -s /manager/susemanager/rhn-conf/rhn_server_susemanager.conf /usr/share/rhn/config-defaults/rhn_server_susemanager.conf
ln -s /manager/backend/rhn-conf/{rhn.conf,rhn_server.conf,rhn_server_satellite.conf} /usr/share/rhn/config-defaults/
ln -s /manager/web/conf/rhn_web.conf /usr/share/rhn/config-defaults/
ln -s /manager/backend /usr/lib64/python3.6/site-packages/spacewalk
ln -s /manager/web/modules/rhn/RHN.pm /usr/lib/perl5/5.26.1/
ln -s /manager/web/modules/rhn/RHN /usr/lib/perl5/5.26.1

mkdir -p /etc/rhn
mkdir -p /usr/share/spacewalk/setup/
