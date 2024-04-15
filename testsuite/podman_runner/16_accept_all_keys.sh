#!/bin/bash
set -xe
sudo -i podman exec uyuni-server bash -c "salt-key -y -A"
