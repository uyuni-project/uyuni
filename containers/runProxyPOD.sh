#!/bin/bash

set -x
CURDIR=$(dirname $(realpath $0))

CONFIG_DIR="$CURDIR/proxy-config"
RUNNER="sudo podman"

$RUNNER pod create --name uyuni_proxy_pod \
        -p 80:80 \
        -p 443:443 \
        -p 4505:4505 \
        -p 4506:4506

$RUNNER run --rm=true -dt --pod uyuni_proxy_pod \
	-e UYUNI_MASTER='suma-refhead-srv.mgr.suse.de' \
	-e UYUNI_MINION_ID='lesch.suse.de' \
	-e UYUNI_CA_CERTS='/config/RHN-ORG-TRUSTED-SSL-CERT' \
	-e UYUNI_SRV_CERT='/config/rhn-org-httpd-ssl-key-pair-lesch-1.0-1.noarch.rpm' \
	-e UYUNI_EMAIL='galaxy-noise@suse.de' \
	-e UYUNI_ACTIVATION_KEY='1-proxy' \
	-e UYUNI_MACHINE_ID='488de1bd7b08472cba12c6e3c775d4bc' \
	-v $CONFIG_DIR:/config \
	-v proxy_www:/srv/www/htdocs/pub \
	-v proxy_squid:/var/cache/squid \
	-v proxy_log:/var/log \
	-v proxy_proxy:/var/spool/rhn-proxy \
	--name uyuni_proxy_main \
        localhost/proxy-main

$RUNNER run --rm=true -dt --pod uyuni_proxy_pod \
	-e UYUNI_MASTER='suma-refhead-srv.mgr.suse.de' \
        -e UYUNI_MINION_ID='lesch.suse.de' \
        -e UYUNI_CA_CERTS='/config/RHN-ORG-TRUSTED-SSL-CERT' \
        -e UYUNI_SRV_CERT='/config/rhn-org-httpd-ssl-key-pair-lesch-1.0-1.noarch.rpm' \
        -e UYUNI_EMAIL='galaxy-noise@suse.de' \
        -e UYUNI_ACTIVATION_KEY='1-proxy' \
        -e UYUNI_MACHINE_ID='488de1bd7b08472cba12c6e3c775d4bc' \
	-v proxy_log:/var/log \
        --name uyuni_proxy_salt_broker \
	localhost/proxy-salt-broker

$RUNNER run --rm=true -dt --pod uyuni_proxy_pod \
        -e UYUNI_MASTER='suma-refhead-srv.mgr.suse.de' \
        -e UYUNI_MINION_ID='lesch.suse.de' \
        -e UYUNI_CA_CERTS='/config/RHN-ORG-TRUSTED-SSL-CERT' \
        -e UYUNI_SRV_CERT='/config/rhn-org-httpd-ssl-key-pair-lesch-1.0-1.noarch.rpm' \
        -e UYUNI_EMAIL='galaxy-noise@suse.de' \
        -e UYUNI_ACTIVATION_KEY='1-proxy' \
        -e UYUNI_MACHINE_ID='488de1bd7b08472cba12c6e3c775d4bc' \
	-v proxy_squid:/var/cache/squid \
	-v proxy_log:/var/log \
        --name uyuni_proxy_squid \
        localhost/proxy-squid

