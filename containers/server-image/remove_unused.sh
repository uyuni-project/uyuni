#!/bin/bash
# Removes any unnecessary files and packages before moving to the next build stage

set -xe

zypper clean --all
rpm -e zypper
