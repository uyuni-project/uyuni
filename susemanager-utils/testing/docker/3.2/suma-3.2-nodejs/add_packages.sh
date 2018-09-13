#!/bin/bash
set -e

# Packages for Javascript tests
zypper --non-interactive in nodejs8 npm8

# Install flow-bin globally
npm_config_prefix=/usr npm install -g flow-bin@0.73
