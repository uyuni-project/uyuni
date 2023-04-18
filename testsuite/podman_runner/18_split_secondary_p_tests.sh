#!/bin/bash
set -xe
export CR=docker
${CR} exec controller-test bash -c "cd /testsuite && rake utils:split_secondary_p[5]"

