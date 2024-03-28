#!/bin/bash
set -xe
sudo -i podman exec uyuni-server-all-in-one-test bash -c "salt-key -y -A"
