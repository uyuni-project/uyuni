#!/bin/bash

find /etc -maxdepth 1 -type f -name '*-' -delete
rm -rf /etc/salt /run/zypp.pid /srv/spm /usr/lib/rpm/macros.d/macros.python3 /usr/lib/sysimage/rpm/* \
       /usr/share/doc /usr/share/licenses /usr/share/man
find /var/log -maxdepth 1 -type f -name '*.log' | xargs -I /bin/bash -c "echo '' > {}"
