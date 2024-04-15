#!/bin/bash
set -xe
sudo -i podman exec controller bash -c "cd /testsuite && rake utils:split_secondary_p[5]"

