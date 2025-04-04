#!/bin/bash
set -xe

sudo -i podman run --privileged --rm -d --network network --name redis -h redis ghcr.io/$UYUNI_PROJECT/uyuni/redis:$UYUNI_VERSION