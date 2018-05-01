#!/bin/bash
set -e

# make sure the package repository is up to date
zypper --non-interactive --gpg-auto-import-keys ref

# this package has a file conflict with python-base
# and this package is not on SLES12 SP1
# a lock prevent the installation
zypper addlock python-strict-tls-check

# Packages required to run the pylint
zypper in -y  make \
              python \
              python-argparse \
              python-base \
              python-configobj \
              python-devel \
              python-mock \
              python-pylint \
              python-urlgrabber \
              python-pyvmomi \
              python-novaclient

zypper -n in vim less

