#!/bin/bash
set -xe
sudo -i podman exec controller bash -c 'export GEM_PATH="/usr/lib64/ruby/gems/3.3.0"'
sudo -i podman exec controller bash -c "cd /testsuite && bundle.ruby3.3 install --gemfile Gemfile --verbose"
sudo -i podman exec controller bash -c "gem env"
sudo -i podman exec controller bash -c "gem list"

# Necessary due to building Twopence from source
sudo -i podman exec controller bash -c "ln -sf /usr/lib64/ruby/gems/3.3.0/gems/twopence-0.4.2/lib/twopence.so /usr/lib64/ruby/gems/3.3.0/gems/twopence-0.4.2/lib/twopence.so.0"
sudo -i podman exec controller bash -c "ldconfig"

# debug
sudo -i podman exec controller bash -c "ldconfig -p | grep twopence"
