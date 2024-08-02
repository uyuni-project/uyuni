#!/bin/bash
set -xe
sudo -i podman exec controller bash -c "cd /testsuite && bundle.ruby3.3 install --gemfile Gemfile --verbose"
