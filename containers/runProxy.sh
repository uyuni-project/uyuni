#!/bin/bash

set -x
CURDIR=$(dirname $(realpath $0))

IMAGE=proxy
CONFIG_DIR="$CURDIR/proxy-config"


docker run --rm=true -ti \
	-e UYUNI_MASTER='suma-refhead-srv.mgr.suse.de' \
	-e UYUNI_ACTIVATON_KEY='1-proxy' \
	-e UYUNI_PROXY_MINION_ID='mc-proxy.suse.de' \
	-e UYUNI_MACHINE_ID='208923e4fc9e46889bd39c4861f0ff0e' \
	-v $CONFIG_DIR:/config \
	--name uyuni_proxy \
        $IMAGE

