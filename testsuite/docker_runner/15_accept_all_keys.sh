#!/bin/bash
set -xe
docker exec uyuni-server-all-in-one-test-1 bash -c "salt-key -y -A"
