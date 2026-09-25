#!/bin/bash

# SPDX-FileCopyrightText: 2026 Red Hat, Inc.
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

# Resolve libs and run tests
cd /manager/java
ant -f manager-build.xml ivy

ant -f manager-build.xml checkstyle

