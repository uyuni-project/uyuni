#!/bin/bash
set -ex
src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

docker run --rm -d --network uyuni-network-1 -v /tmp/test-all-in-one:/tmp --name controller-test-1 -h controller-test-1 -v ${src_dir}/testsuite:/testsuite ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-controller-dev:$UYUNI_VERSION

# sleep 10
docker ps
