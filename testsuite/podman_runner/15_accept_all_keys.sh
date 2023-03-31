#!/bin/bash
set -xe
podman exec uyuni-server-all-in-one-test bash -c "salt-key -y -A"
