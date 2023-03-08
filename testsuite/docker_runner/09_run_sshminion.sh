#!/bin/bash
set -xe
docker run --rm -d --network uyuni-network-1 -v /tmp/test-all-in-one:/tmp --name opensusessh -h opensusessh ghcr.io/$UYUNI_PROJECT/uyuni/opensuse-minion:$UYUNI_VERSION
# sleep 10

