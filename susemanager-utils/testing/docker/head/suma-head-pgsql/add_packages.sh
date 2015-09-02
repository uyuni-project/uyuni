#!/bin/bash
set -e

# make sure the package repository is up to date
zypper --non-interactive --gpg-auto-import-keys ref

# Packages required to run spacewalk-setup inside of the container
zypper in -y postgresql94-contrib \
             postgresql94-server \
             postgresql94-pltcl \
             smdba \
             perl-DBD-Pg
