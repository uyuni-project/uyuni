#!/bin/bash
# Removes any unnecessary files and packages before moving to the next build stage

set -xe

# remove rpm-build and its dependencies
rpm -e rpm-build --nodeps
rpm -e dwz make gcc patch diffutils python-rpm-macros systemd-rpm-macros glibc-locale

# remove perl and its dependencies
rpm -e --nodeps perl spacewalk-base-minimal
rpm -e perl-DBI perl-Module-Implementation perl-Module-Runtime perl-Params-Validate perl-Try-Tiny

# remove locale data
rm -rf /usr/share/locale

# remove other packages
zypper --non-interactive rm \
  binutils \
  cpp7 \
  glibc-locale-base

# remove packages with a -devel suffix
zypper --non-interactive rm *-devel

zypper clean --all