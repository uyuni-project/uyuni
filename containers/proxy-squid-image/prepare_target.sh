#!/bin/bash
# Moves to /target any file to be preserved in the slimmed-down image

set -xe

mkdir -p /target/etc/
mv /etc/passwd /target/etc
mv /etc/group /target/etc

mkdir -p /target/etc/squid
mv /etc/squid /target/etc

mkdir -p /target/etc/pam.d/squid
mv /etc/pam.d/squid /target/etc/pam.d/squid

mkdir -p /target/usr/bin
mv /usr/bin/uyuni-configure.py /target/usr/bin/

mkdir -p /target/usr/sbin/squid
mv /usr/sbin/squid /target/usr/sbin/squid

if test $(rpm -ql squid | grep lib64); then
    mkdir -p /target/usr/lib64/squid
    mv /usr/lib64/squid /target/usr/lib64
fi

mkdir -p /target/usr/lib/squid
mv /usr/lib/squid /target/usr/lib

mkdir -p /target/var/cache/squid
mv /var/cache/squid /target/var/cache

mkdir -p /target/var/log/squid
