#!/bin/bash
set -xe
sudo -i podman exec controller bash -c "cd /testsuite && bundle.ruby2.5 install --gemfile Gemfile"
