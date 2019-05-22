#!/bin/bash
set -e

# Packages required to run the pylint
zypper in -y  make \
              python3 \
	      python3-PyYAML \
              python3-base \
              python3-configobj \
              python3-devel \
              python3-mock \
	      python3-nose \
              python3-pylint \
              python3-urlgrabber

zypper -n in vim less

