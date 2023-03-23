#!/bin/bash
set -xe
docker exec uyuni-server-all-in-one-test bash -c "salt-key -y -A"
