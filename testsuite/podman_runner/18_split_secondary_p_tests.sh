#!/bin/bash
set -xe
podman exec controller-test bash -c "cd /testsuite && rake utils:split_secondary_p[5]"

