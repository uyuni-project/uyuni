#!/bin/bash
set -e

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

