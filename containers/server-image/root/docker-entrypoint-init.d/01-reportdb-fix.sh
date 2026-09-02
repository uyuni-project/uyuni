#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

# Disable SSL on the internal DB network (bsc#1277589).
#
# The DB container serves a certificate whose SANs only cover the FQDN,
# but the server connects via the internal aliases 'db' / 'reportdb',
# so verify-full cannot succeed. Force SSL off when talking to the
# internal DB, otherwise uyuni-check-database fails on startup with:
#     LOG: could not accept SSL connection: tlsv1 alert unknown ca
if grep -q "^db_host\s*=\s*db\s*$" /etc/rhn/rhn.conf; then
    sed -i -e "s|^db_ssl_enabled.*$|db_ssl_enabled = |" /etc/rhn/rhn.conf
fi

if grep -q "^report_db_host\s*=\s*reportdb\s*$" /etc/rhn/rhn.conf; then
    sed -i -e "s|^report_db_ssl_enabled.*$|report_db_ssl_enabled = |" /etc/rhn/rhn.conf
fi

if grep -q "^report_db_sslrootcert\s*=\s*/etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT$" /etc/rhn/rhn.conf; then
    sed -i -e "s|^report_db_sslrootcert\s*=\s*/etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT$|report_db_sslrootcert = /etc/pki/trust/anchors/DB-RHN-ORG-TRUSTED-SSL-CERT|" /etc/rhn/rhn.conf
fi
