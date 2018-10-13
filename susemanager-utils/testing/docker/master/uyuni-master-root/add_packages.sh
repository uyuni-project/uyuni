#!/bin/bash
set -e

# Packages required to run the pylint
zypper in -y  make \
              python \
	      python-PyYAML \
              python-argparse \
              python-base \
              python-configobj \
              python-devel \
              python-mock \
	      python-nose \
              python-pylint \
              python-urlgrabber

zypper -n in vim less

