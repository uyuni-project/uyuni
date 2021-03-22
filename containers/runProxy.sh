#!/bin/bash

set -x

IMAGE=proxy

docker run --rm=true -ti \
	-e UYUNI_MASTER='suma-refhead-srv.mgr.suse.de' \
	-e UYUNI_ACTIVATON_KEY='1-proxy' \
	--name uyuni_proxy \
        $IMAGE

