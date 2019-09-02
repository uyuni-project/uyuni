#!/bin/bash -e

# nobody user is required for npm but we may already have it
if test ! $(getent passwd nobody); then
    useradd --no-create-home --shell /dev/null nobody
fi

# Yarn is required for susemanager-nodejs-sdk-devel
npm install -g yarn@v1.9.4
