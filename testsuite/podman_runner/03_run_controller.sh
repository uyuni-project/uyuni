#!/bin/bash
set -ex
src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

sudo -i podman run --rm -d --network uyuni-network-1 -v /tmp/test-all-in-one:/tmp --name controller-test -h controller-test -v ${src_dir}/testsuite:/testsuite ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-controller-dev:$UYUNI_VERSION

podman ps
