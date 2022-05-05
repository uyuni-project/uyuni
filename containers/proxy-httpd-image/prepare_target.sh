#!/bin/bash
# Moves to /target any file to be preserved in the slimmed-down image

set -xe

# rhn config
mkdir -p /target/etc/rhn
mkdir -p /target/usr/share/rhn/config-defaults
mv /etc/rhn/rhn*.conf /target/etc/rhn/rhn.conf
mv /usr/share/rhn/config-defaults/rhn*.conf /target/usr/share/rhn/config-defaults

# apache2 and sysconfig
mkdir -p /target/etc/apache2
mkdir -p /target/etc/sysconfig
mv /etc/apache2 /target/etc/
mv /etc/sysconfig /target/etc/

# rhn proxy
mkdir -p /target/usr/share/rhn/proxy
mv /usr/share/rhn/proxy /target/usr/share/rhn/

# uyuni-configure.py
mkdir -p /target/usr/bin
mv /usr/bin/uyuni-configure.py /target/usr/bin/

# site packages
SITE_PACKAGES=usr/lib/$(ls /usr/lib/ | grep python)/site-packages
mkdir -p /target/$SITE_PACKAGES
mv /$SITE_PACKAGES/spacewalk /target/$SITE_PACKAGES
mv /$SITE_PACKAGES/rhn /target/$SITE_PACKAGES
mv /$SITE_PACKAGES/uyuni /target/$SITE_PACKAGES

mkdir -p /target/var/log/salt
