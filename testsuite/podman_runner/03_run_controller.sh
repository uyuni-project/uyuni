#!/bin/bash
set -ex
src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

sudo -i podman run --rm -d --network uyuni-network -v /tmp/testing:/tmp --name uyuni-controller -h uyuni-controller -v ${src_dir}/testsuite:/testsuite ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-controller-dev:$UYUNI_VERSION

sudo podman ps
