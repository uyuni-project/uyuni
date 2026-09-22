#! /bin/bash

# SPDX-FileCopyrightText: 2026 Red Hat, Inc.
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

python3 -m venv venv
venv/bin/pip install flask pytest pyOpenSSL
ln -s ../spacewalk .
ln -s ../uyuni .
ln -s ../rhn .
echo "venv/bin/python3 -m pytest -s tests/"
