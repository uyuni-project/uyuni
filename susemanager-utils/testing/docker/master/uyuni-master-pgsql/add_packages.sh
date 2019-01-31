#!/bin/bash
set -e

# make sure the package repository is up to date
zypper --non-interactive --gpg-auto-import-keys ref

# Packages required to run spacewalk-setup inside of the container
zypper in -y postgresql10-contrib \
             postgresql10-server \
             smdba \
             perl-DBD-Pg \
             python3-M2Crypto
