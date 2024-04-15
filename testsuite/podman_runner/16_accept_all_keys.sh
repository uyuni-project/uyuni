#!/bin/bash
set -xe
sudo -i podman exec server bash -c "salt-key -y -A"
