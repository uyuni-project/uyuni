#!/bin/bash
# Removes any unnecessary files and packages before moving to the next build stage

set -xe

<<<<<<< HEAD
=======
# remove rpm-build and its dependencies
rpm -e diffutils

>>>>>>> 2ad6b420a6d9d7f5ddd431d26346efbe2f72b840
# remove perl and its dependencies
rpm -e --nodeps perl

# remove locale data
rm -rf /usr/share/locale

zypper clean --all
<<<<<<< HEAD

=======
>>>>>>> 2ad6b420a6d9d7f5ddd431d26346efbe2f72b840
