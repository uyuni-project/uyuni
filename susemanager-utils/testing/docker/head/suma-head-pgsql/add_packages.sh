#!/bin/bash
set -e

# Packages required to run spacewalk-setup inside of the container
zypper in -y postgresql10-contrib \
             postgresql10-server \
             smdba \
             perl-DBD-Pg \
             python3-M2Crypto
