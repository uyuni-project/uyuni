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


mkdir -p /tmp/cobblertest/
mkdir -p /var/lib/cobbler/kickstarts
mkdir -p /usr/share/cobbler/web/

touch /tmp/cobblertest/fake-kernel
touch /tmp/cobblertest/fake-initrd
touch /var/lib/cobbler/kickstarts/sample.ks
touch /usr/share/cobbler/web/cobbler.wsgi


