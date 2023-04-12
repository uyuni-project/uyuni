#!/bin/bash
set -xe
podman run --rm -d --network uyuni-network-1 -v /run/udev/data:/run/udev/data -v /tmp/test-all-in-one:/tmp --name opensusessh -h opensusessh ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-opensuse-minion:$UYUNI_VERSION

