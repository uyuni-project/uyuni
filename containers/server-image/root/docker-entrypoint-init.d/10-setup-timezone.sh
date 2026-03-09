#!/usr/bin/bash

if [ -n "${TZ}" ]; then
    rm -f /etc/localtime
    ln -s "/usr/share/zoneinfo/${TZ}" /etc/localtime
fi
