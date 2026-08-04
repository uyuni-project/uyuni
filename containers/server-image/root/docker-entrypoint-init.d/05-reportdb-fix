#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

# Fix incorrectly configured report-db CA (bsc#1260806)
if grep -q "^report_db_sslrootcert\s*=\s*/etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT$" /etc/rhn/rhn.conf; then
    sed -i -e "s|^report_db_sslrootcert\s*=\s*/etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT$|report_db_sslrootcert = /etc/pki/trust/anchors/DB-RHN-ORG-TRUSTED-SSL-CERT|" /etc/rhn/rhn.conf
fi
