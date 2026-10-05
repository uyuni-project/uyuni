#! /bin/bash

# SPDX-FileCopyrightText: 2026 Red Hat, Inc.
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

pushd $(dirname $0)
cp ../../../../../../../../../susemanager-sync-data/additional_products.json .
cp ../../../../../../../../../susemanager-sync-data/additional_repositories.json .
cp ../../../../../../../../../susemanager-sync-data/channel_families.json .
cp ../../../../../../../../../susemanager-sync-data/product_tree.json .
cp ../../../../../../../../../susemanager-sync-data/upgrade_paths.json .
popd
