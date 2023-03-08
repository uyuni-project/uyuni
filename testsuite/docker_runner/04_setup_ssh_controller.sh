#!/bin/bash
set -ex
docker exec controller-test-1 bash -c "ssh-keygen -f /root/.ssh/id_rsa -t rsa -N \"\" && cp /root/.ssh/id_rsa.pub /tmp/authorized_keys"

