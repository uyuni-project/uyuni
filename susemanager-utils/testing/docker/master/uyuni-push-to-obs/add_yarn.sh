#!/bin/bash -e

# nobody user is required for npm
useradd --no-create-home --shell /dev/null nobody

# Yarn is required for susemanager-nodejs-sdk-devel
npm install -g yarn@v1.9.4
