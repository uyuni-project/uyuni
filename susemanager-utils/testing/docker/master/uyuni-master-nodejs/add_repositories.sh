#!/bin/bash
set -e

zypper ar -f http://download.opensuse.org/repositories/devel:/languages:/nodejs/openSUSE_Leap_42.3/ "nodejs"

zypper --non-interactive --gpg-auto-import-keys ref nodejs
