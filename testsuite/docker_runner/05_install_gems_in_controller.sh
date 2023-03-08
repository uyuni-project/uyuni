#!/bin/bash
set -xe
docker exec controller-test-1 bash -c "cd /testsuite && bundle.ruby2.5 install --gemfile Gemfile"

