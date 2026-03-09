#!/usr/bin/bash
sed 's/cobbler\.host.*/cobbler\.host = localhost/' -i /etc/rhn/rhn.conf;
if [ -f /etc/cobbler/settings.d/zz-uyuni.settings ] && \
    grep -q uyuni_authentication_endpoint /etc/cobbler/settings.d/zz-uyuni.settings; then
	sed 's/uyuni_authentication_endpoint.*/uyuni_authentication_endpoint: http:\/\/localhost/' \
        -i /etc/cobbler/settings.d/zz-uyuni.settings;
else
	echo 'uyuni_authentication_endpoint: "http://localhost"' >> /etc/cobbler/settings.d/zz-uyuni.settings
fi

if grep -q pam_auth_service /etc/rhn/rhn.conf; then
	sed 's/pam_auth_service.*/pam_auth_service = susemanager/' -i /etc/rhn/rhn.conf;
else
	echo 'pam_auth_service = susemanager' >> /etc/rhn/rhn.conf
fi

if test -e /etc/sysconfig/prometheus-postgres_exporter/systemd/60-server.conf; then
        sed 's/\/etc\/postgres_exporter\//\/etc\/sysconfig\/prometheus-postgres_exporter\//' \
        -i /etc/sysconfig/prometheus-postgres_exporter/systemd/60-server.conf;
fi

