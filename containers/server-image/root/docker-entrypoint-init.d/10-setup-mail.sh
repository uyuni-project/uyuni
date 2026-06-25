#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only
set -e

# Update postfix hostname only when hostname is provided
if [ -n "$UYUNI_HOSTNAME" ]; then
    postconf -e "myhostname=${UYUNI_HOSTNAME}"
fi
