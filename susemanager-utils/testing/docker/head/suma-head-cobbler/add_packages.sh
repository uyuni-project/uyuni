#!/bin/bash
set -e

# make sure the package repository is up to date
zypper --non-interactive --gpg-auto-import-keys ref

# Packages required to run the cobbler unit tests
zypper in -y  --no-recommends apache2 \
                              python \
                              python-PyYAML \
                              python-devel \
                              python-mock \
                              python-nose \
                              python-unittest2 \
                              virt-install
