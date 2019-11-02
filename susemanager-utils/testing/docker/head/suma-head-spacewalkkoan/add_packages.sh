#!/bin/bash
set -e

# Packages required to run spacewalk-setup inside of the container
zypper in -y file gzip koan
