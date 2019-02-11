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
              python3-pylint

# python3-urlgabber is not part of neither SLE or openSUSE 15.X
curl https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master/openSUSE_Leap_15.0/noarch/python3-urlgrabber.rpm -o /tmp/python3-urlgrabber.rpm && \
rpm -i /tmp/python3-urlgrabber.rpm && \
rm -rf python3-urlgrabber.rpm

zypper -n in vim less

