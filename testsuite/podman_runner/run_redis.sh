#!/bin/bash
set -xe

sudo -i podman run --privileged --rm -d --name redis -h redis -p 6379:6379 ghcr.io/$UYUNI_PROJECT/uyuni/redis:$UYUNI_VERSION