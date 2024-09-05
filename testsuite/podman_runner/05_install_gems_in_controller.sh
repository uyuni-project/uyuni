#!/bin/bash
set -xe
sudo -i podman exec controller bash -c 'export GEM_PATH="/usr/lib64/ruby/gems/3.3.0"'
sudo -i podman exec controller bash -c "cd /testsuite && bundle.ruby3.3 install --gemfile Gemfile --verbose"
sudo -i podman exec controller bash -c "gem env"
sudo -i podman exec controller bash -c "gem list"
