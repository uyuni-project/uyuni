#!/bin/bash
set -e

# make sure the package repository is up to date
zypper --non-interactive --gpg-auto-import-keys ref

# Packages required to run the cobbler unit tests
zypper -n in  --no-recommends apache2 \
                              koan \
                              python \
                              python-PyYAML \
                              python-devel \
                              python-mock \
                              python-nose \
                              virt-install

zypper -n in vim less
