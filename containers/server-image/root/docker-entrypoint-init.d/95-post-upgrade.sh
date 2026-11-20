#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

sed -i 's/cobbler\.host.*/cobbler\.host = localhost/' /etc/rhn/rhn.conf
if [ -f /etc/cobbler/settings.d/zz-uyuni.settings ] &&
    grep -q uyuni_authentication_endpoint /etc/cobbler/settings.d/zz-uyuni.settings; then
    sed -i 's/uyuni_authentication_endpoint.*/uyuni_authentication_endpoint: http:\/\/localhost/' \
        /etc/cobbler/settings.d/zz-uyuni.settings
else
    echo 'uyuni_authentication_endpoint: "http://localhost"' >> /etc/cobbler/settings.d/zz-uyuni.settings
fi

if [ -e /etc/sysconfig/prometheus-postgres_exporter/systemd/60-server.conf ]; then
    sed -i 's/\/etc\/postgres_exporter\//\/etc\/sysconfig\/prometheus-postgres_exporter\//' \
        /etc/sysconfig/prometheus-postgres_exporter/systemd/60-server.conf
fi

# JMX settings moved from systemd drop-ins to conf.d files
if [ -s /etc/sysconfig/tomcat/systemd/jmx.conf ] &&
    [ ! -e /etc/tomcat/conf.d/tomcat_jmx.conf ]; then
    mkdir -p /etc/tomcat/conf.d
    cp /usr/share/susemanager/salt/srvmonitoring/tomcat_jmx.conf /etc/tomcat/conf.d/
fi
if [ -s /etc/sysconfig/taskomatic/systemd/jmx.conf ] &&
    [ ! -e /etc/rhn/taskomatic.conf.d/taskomatic_jmx.conf ]; then
    mkdir -p /etc/rhn/taskomatic.conf.d
    cp /usr/share/susemanager/salt/srvmonitoring/taskomatic_jmx.conf /etc/rhn/taskomatic.conf.d/
fi
rm -f /etc/sysconfig/tomcat/systemd/jmx.conf /etc/sysconfig/taskomatic/systemd/jmx.conf
