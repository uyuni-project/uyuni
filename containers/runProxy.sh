#!/bin/bash

set -x
CURDIR=$(dirname $(realpath $0))

IMAGE=localhost/proxy
CONFIG_DIR="$CURDIR/proxy-config"
RUNNER=podman
STORAGE=/fast/mc/

if [ ! -d $STORAGE/www ]; then
	mkdir -p $STORAGE/www
fi
if [ ! -d $STORAGE/squid ]; then
	mkdir -p $STORAGE/squid
fi
if [ ! -d $STORAGE/log ]; then
	mkdir -p $STORAGE/log
fi
if [ ! -d $STORAGE/proxy ]; then
	mkdir -p $STORAGE/proxy
fi

$RUNNER run --rm=true -ti \
	-e UYUNI_MASTER='suma-refhead-srv.mgr.suse.de' \
	-e UYUNI_ACTIVATION_KEY='1-proxy' \
	-e UYUNI_MINION_ID='mc-proxy.suse.de' \
	-e UYUNI_MACHINE_ID='488de1bd7b08472cba12c6e3c775d4bb' \
	-e UYUNI_CA_CERTS='/config/RHN-ORG-TRUSTED-SSL-CERT' \
	-e UYUNI_SRV_CERT='/config/'`find $CONFIG_DIR -name "rhn-org-httpd-ssl-key-pair-*.rpm" -printf "%f"` \
	-v $CONFIG_DIR:/config \
	-v $STORAGE/www:/srv/www/htdocs/pub \
	-v $STORAGE/squid:/var/cache/squid \
	-v $STORAGE/log:/var/log_perm \
	-v $STORAGE/proxy:/var/spool/rhn-proxy \
	-p 80:80 \
	-p 443:443 \
	-p 4505:4505 \
	-p 4506:4506 \
	--name uyuni_proxy \
        $IMAGE \
		/bin/bash
