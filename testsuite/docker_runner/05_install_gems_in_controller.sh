#!/bin/bash
set -xe
docker exec controller-test bash -c "cd /testsuite && bundle.ruby2.5 install --gemfile Gemfile"

