# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

#/bin/bash -e
echo "*************** CLEANING DIRECTORIES GENERATED AS USER ROOT  ***************"
rm -rf web/node_modules
rm -rf web/html/src/dist
rm -f web/node-modules.tar.gz
rm -rf rel-eng/custom/*
