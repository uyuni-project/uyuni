#!/bin/bash
set -e

# Packages required to run the cobbler unit tests
zypper -n in  --no-recommends apache2 \
                              koan \
                              virt-install \
                              python3-pip
