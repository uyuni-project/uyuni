#!/bin/bash
set -e

# Uninstall Java 7 completely
rpm -e --nodeps java-1_7_0-ibm-devel java-1_7_0-ibm

# Install IBM JDK 8
zypper ar -f http://download.suse.de/ibs/home:/scarabeus_iv/SLE12/ "home:scarabeus_iv"
zypper --non-interactive --gpg-auto-import-keys ref
zypper in -y java-1_8_0-ibm-devel

