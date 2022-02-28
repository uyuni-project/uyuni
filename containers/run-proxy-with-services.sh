#!/bin/bash

set -x

CURDIR=$(dirname $(realpath $0))
CONFIG_DIR="$CURDIR/proxy-config"
SQUID_CACHE_DIR="$CURDIR/proxy-squid-cache"
RHN_CACHE_DIR="$CURDIR/proxy-rhn-cache"
TFTPBOOT_DIR="$CURDIR/proxy-tftpboot"

export REGISTRY=registry.tf.local

# HACK: overcome the inavailability of avahi in containers
export ADD_HOST=server.tf.local:192.168.122.254
export ADD_CLIENT=client.tf.local:192.168.122.89

IMAGES=(proxy-ssh proxy-httpd proxy-salt-broker proxy-squid proxy-tftpd)

for image in "${IMAGES[@]}"
do
  podman pull --tls-verify=false $REGISTRY/$image
done

###
# POD
###
podman pod create --name proxy-pod \
  --publish 22:22 \
  --publish 8080:8080 \
  --publish 443:443 \
  --publish 4505:4505 \
  --publish 4506:4506 \
  --add-host $ADD_HOST \
  --add-host $ADD_CLIENT

###
# CONTAINERS
###
podman create -dt --pod proxy-pod \
  -v $CONFIG_DIR:/etc/uyuni \
  --name proxy-ssh \
  $REGISTRY/proxy-ssh

podman create -dt --pod proxy-pod \
  -v $CONFIG_DIR:/etc/uyuni \
  -v $RHN_CACHE_DIR:/var/cache/rhn \
  -v $TFTPBOOT_DIR:/srv/tftpboot \
  --name proxy-httpd \
  $REGISTRY/proxy-httpd

podman create -dt --pod proxy-pod \
  -v $CONFIG_DIR:/etc/uyuni \
  --name proxy-salt-broker \
  $REGISTRY/proxy-salt-broker

podman create -d --pod proxy-pod \
  -v $CONFIG_DIR:/etc/uyuni \
  -v $SQUID_CACHE_DIR:/var/cache/squid \
  --name proxy-squid \
  $REGISTRY/proxy-squid

podman create -dt --pod proxy-pod \
  -v $CONFIG_DIR:/etc/uyuni \
  --name proxy-tftpd \
  $REGISTRY/proxy-tftpd


# generate systemd services
podman generate systemd --files --name --new proxy-pod
# replace KillMode=none with TimeoutStopSec=60 as per https://github.com/containers/podman/pull/8889
sed -i 's/KillMode=none/TimeoutStopSec=60/' *-proxy-*.service

mv *-proxy-*.service /etc/systemd/system/.

# start services
systemctl daemon-reload
systemctl start pod-proxy-pod.service
