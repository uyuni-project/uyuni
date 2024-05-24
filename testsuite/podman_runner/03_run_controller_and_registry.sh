#!/bin/bash
set -ex
src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

sudo -i podman run --rm -d --network network -v /tmp/testing:/tmp --name controller -h controller -v ${src_dir}/testsuite:/testsuite ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-controller-dev:$UYUNI_VERSION
sudo -i podman run --rm -d --network network --name $NO_AUTH_REGISTRY -h $NO_AUTH_REGISTRY ghcr.io/$UYUNI_PROJECT/uyuni/ci-container-registry:$UYUNI_VERSION

sudo podman ps
