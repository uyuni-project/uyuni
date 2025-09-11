#!/bin/bash
# Moves to /target any file to be preserved in the slimmed-down image

set -xe

mkdir -p /target/etc/rhn
mv /etc/rhn/rhn*.conf /target/etc/rhn/rhn.conf

mkdir -p /target/etc/salt
mv /etc/salt/broker /target/etc/salt

mkdir -p /target/usr/share/rhn/config-defaults
mv /usr/share/rhn/config-defaults/rhn*.conf /target/usr/share/rhn/config-defaults

mkdir -p /target/usr/bin
mv /usr/bin/salt-broker /target/usr/bin/
mv /usr/bin/uyuni-configure.py /target/usr/bin/

for pyver in $(ls /usr/lib/ | grep python); do
    if [ -d /usr/lib/$pyver/site-packages/spacewalk ] && [ -d /usr/lib/$pyver/site-packages/spacewalk ] && [ -d /usr/lib/$pyver/site-packages/spacewalk ]; then
        SITE_PACKAGES=usr/lib/$pyver/site-packages
    fi
done

if [ -z $SITE_PACKAGES ]; then
    echo "ERROR CANNOT FIND CONSISTENT PLACE FOR UYUNI-COMMON-LIBS"
    exit 1
fi

mkdir -p /target/$SITE_PACKAGES
mv /$SITE_PACKAGES/spacewalk /target/$SITE_PACKAGES
mv /$SITE_PACKAGES/rhn /target/$SITE_PACKAGES
mv /$SITE_PACKAGES/uyuni /target/$SITE_PACKAGES

mkdir -p /target/var/log/salt
