#!/bin/bash

set -x
CURDIR=$(dirname $(realpath $0))

IMAGE=localhost/proxy
CONFIG_DIR="$CURDIR/proxy-config"
RUNNER=podman

$RUNNER run --rm=true -ti \
	-e UYUNI_MASTER='suma-refhead-srv.mgr.suse.de' \
	-e UYUNI_ACTIVATION_KEY='1-proxy' \
	-e UYUNI_MINION_ID='mc-proxy.suse.de' \
	-e UYUNI_MACHINE_ID='488de1bd7b08472cba12c6e3c775d4bb' \
	-e UYUNI_CA_CERTS='/config/RHN-ORG-TRUSTED-SSL-CERT' \
	-e UYUNI_SRV_CERT='/config/rhn-org-httpd-ssl-key-pair-proxy-1.0-1.noarch.rpm' \
	-v $CONFIG_DIR:/config \
	--name uyuni_proxy \
        $IMAGE \
		/bin/bash
