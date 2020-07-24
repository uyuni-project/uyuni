#!/bin/bash
set -e

# Packages required to run the pylint
zypper in -y  python3-pyvmomi \
              python3-novaclient

