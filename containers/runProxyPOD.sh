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
	--env-file=$CONFIG_DIR/environment \
	-v $CONFIG_DIR:/config \
	-v proxy_www:/srv/www/htdocs/pub \
	-v proxy_squid:/var/cache/squid \
	-v proxy_log:/var/log \
	-v proxy_proxy:/var/spool/rhn-proxy \
	--name uyuni_proxy_main \
        localhost/proxy-main

$RUNNER run --rm=true -dt --pod uyuni_proxy_pod \
	--env-file=$CONFIG_DIR/environment \
	-v proxy_log:/var/log \
        --name uyuni_proxy_salt_broker \
	localhost/proxy-salt-broker

$RUNNER run --rm=true -dt --pod uyuni_proxy_pod \
	--env-file=$CONFIG_DIR/environment \
	-v proxy_squid:/var/cache/squid \
	-v proxy_log:/var/log \
        --name uyuni_proxy_squid \
        localhost/proxy-squid

