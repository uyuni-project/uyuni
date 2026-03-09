#!/usr/bin/bash
# In the container case, we have the MIRROR_PATH environment variable at setup
if [ -n "${MIRROR_PATH}" ]; then
    if ! grep -q "^server.susemanager.fromdir = ${MIRROR_PATH}" /etc/rhn/rhn.conf; then
        sed "s/^server.susemanager.fromdir =/#server.susemanager.fromdir =/" -i /etc/rhn/rhn.conf
        echo "server.susemanager.fromdir = ${MIRROR_PATH}" >> /etc/rhn/rhn.conf
    fi
elif grep -q "^server.susemanager.fromdir =" /etc/rhn/rhn.conf; then
    sed "s/^server.susemanager.fromdir =/#server.susemanager.fromdir =/" -i /etc/rhn/rhn.conf
fi
