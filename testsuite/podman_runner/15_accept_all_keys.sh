#!/bin/bash
set -xe
export CR=docker
${CR} exec uyuni-server-all-in-one-test bash -c "salt-key -y -A"
