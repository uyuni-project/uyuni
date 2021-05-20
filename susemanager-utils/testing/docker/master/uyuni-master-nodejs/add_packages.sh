#!/bin/bash
set -e

# Required by nodejs
useradd --no-create-home --shell /dev/null -c '' nobody

# Packages for Javascript tests
zypper --non-interactive in nodejs npm
