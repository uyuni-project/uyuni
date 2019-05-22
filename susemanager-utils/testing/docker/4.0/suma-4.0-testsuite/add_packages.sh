#!/bin/bash
set -e

# make sure the package repository is up to date
zypper --non-interactive --gpg-auto-import-keys ref

zypper --non-interactive install --no-recommends andromeda-dummy milkyway-dummy virgo-dummy
