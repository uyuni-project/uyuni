#!/bin/bash
set -ex

sudo -i podman pull "ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-controller-dev:$UYUNI_VERSION"
#sudo -i podman pull "ghcr.io/$UYUNI_PROJECT/uyuni/ci-buildhost:$UYUNI_VERSION"
sudo -i podman pull "ghcr.io/$UYUNI_PROJECT/uyuni/ci-container-registry-auth:$UYUNI_VERSION"
sudo -i podman pull "ghcr.io/$UYUNI_PROJECT/uyuni/ci-container-registry:$UYUNI_VERSION"
sudo -i podman pull "ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-server-all-in-one-dev:$UYUNI_VERSION"
sudo -i podman pull "ghcr.io/$UYUNI_PROJECT/uyuni/ci-postgresql:$UYUNI_VERSION"
