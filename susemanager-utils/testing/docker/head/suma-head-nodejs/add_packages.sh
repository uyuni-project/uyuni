#!/bin/bash
set -e

# Required by nodejs
useradd --no-create-home --shell /dev/null -c '' nobody

# Packages for Javascript tests
zypper --non-interactive in nodejs npm

# Install flow-bin globally
npm_config_prefix=/usr npm install -g flow-bin@0.82.0
