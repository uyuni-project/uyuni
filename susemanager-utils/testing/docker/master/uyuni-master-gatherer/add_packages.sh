#!/bin/bash
set -e

# Packages required to run the pylint
zypper in -y  python-pyvmomi \
              python-novaclient

