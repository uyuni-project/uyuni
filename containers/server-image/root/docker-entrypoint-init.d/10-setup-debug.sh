#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only
: "${DEBUG_JAVA:=false}"

if [ "${DEBUG_JAVA}" = "true" ]; then
    echo "Setting up Java debug options"
    # Note: $JAVA_OPTS inside single quotes is NOT expanded here.
    # It assumes the target file is a shell script that will source this line later.
    if [ ! -f /etc/tomcat/conf.d/remote_debug.conf ] || ! grep -q 'Xrunjdwp' /etc/tomcat/conf.d/remote_debug.conf; then
        echo 'JAVA_OPTS=" $JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=*:8003,server=y,suspend=n" ' >> /etc/tomcat/conf.d/remote_debug.conf
    fi
    if [ ! -f /etc/rhn/taskomatic.conf ] || ! grep -q 'Xrunjdwp' /etc/rhn/taskomatic.conf; then
        echo 'JAVA_OPTS=" $JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=*:8001,server=y,suspend=n" ' >> /etc/rhn/taskomatic.conf
    fi
    if [ ! -f /etc/rhn/rhn_search_daemon.conf ] || ! grep -q 'Xrunjdwp' /etc/rhn/rhn_search_daemon.conf; then
        echo 'JAVA_OPTS=" $JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=*:8002,server=y,suspend=n" ' >> /etc/rhn/rhn_search_daemon.conf
    fi
else
    echo "Removing Java debug options"
    sed -i '/Xrunjdwp/d' /etc/tomcat/conf.d/remote_debug.conf 2> /dev/null || :
    sed -i '/Xrunjdwp/d' /etc/rhn/taskomatic.conf 2> /dev/null || :
    sed -i '/Xrunjdwp/d' /etc/rhn/rhn_search_daemon.conf 2> /dev/null || :
fi
