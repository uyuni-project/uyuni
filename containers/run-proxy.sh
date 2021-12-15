#!/bin/bash

set -x

CURDIR=$(dirname $(realpath $0))
CONFIG_DIR="$CURDIR/proxy-config"
SQUID_CACHE_DIR="$CURDIR/proxy-squid-cache"
RHN_CACHE_DIR="$CURDIR/proxy-rhn-cache"
TFTPBOOT_DIR="$CURDIR/proxy-tftpboot"

export REGISTRY=registry.tf.local

# HACK: overcome the inavailability of avahi in containers
export ADD_HOST=server.tf.local:192.168.100.189


IMAGES=(proxy-httpd proxy-salt-broker proxy-squid proxy-tftpd)

for image in "${IMAGES[@]}"
do
	podman pull --tls-verify=false $REGISTRY/$image
done

podman pod create --name proxy-pod \
        --publish 8080:8080 \
        --publish 443:443 \
        --publish 4505:4505 \
        --publish 4506:4506 \
				--add-host $ADD_HOST

podman run --rm=true -dt --pod proxy-pod \
	-v $CONFIG_DIR:/etc/uyuni \
	-v $RHN_CACHE_DIR:/var/cache/rhn \
	-v $TFTPBOOT_DIR:/srv/tftpboot \
	--name proxy-httpd \
	$REGISTRY/proxy-httpd

podman run --rm=true -dt --pod proxy-pod \
	-v $CONFIG_DIR:/etc/uyuni \
	--name proxy-salt-broker \
	$REGISTRY/proxy-salt-broker

podman run --rm=true -dt --pod proxy-pod \
	-v $CONFIG_DIR:/etc/uyuni \
	-v $SQUID_CACHE_DIR:/var/cache/squid \
	--name proxy-squid \
	$REGISTRY/proxy-squid

podman run --rm=true -dt --pod proxy-pod \
	-v $CONFIG_DIR:/etc/uyuni \
	--name proxy-tftpd \
	$REGISTRY/proxy-tftpd
