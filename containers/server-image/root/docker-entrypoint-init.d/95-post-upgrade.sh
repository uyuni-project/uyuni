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

if grep -q pam_auth_service /etc/rhn/rhn.conf; then
    sed -i 's/pam_auth_service.*/pam_auth_service = susemanager/' /etc/rhn/rhn.conf
else
    echo 'pam_auth_service = susemanager' >> /etc/rhn/rhn.conf
fi

if [ -e /etc/sysconfig/prometheus-postgres_exporter/systemd/60-server.conf ]; then
    sed -i 's/\/etc\/postgres_exporter\//\/etc\/sysconfig\/prometheus-postgres_exporter\//' \
        /etc/sysconfig/prometheus-postgres_exporter/systemd/60-server.conf
fi
