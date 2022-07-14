#!/bin/bash
# Removes any unnecessary files and packages before moving to the next build stage

set -xe

# remove rpm-build and its dependencies
rpm -e diffutils

# remove perl and its dependencies
rpm -e --nodeps perl

# remove locale data
rm -rf /usr/share/locale

zypper clean --all
