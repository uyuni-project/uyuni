#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

mkdir -p "/var/spacewalk/pqkeys"
chown -R tomcat:susemanager "/var/spacewalk/pqkeys"
chmod 0700 "/var/spacewalk/pqkeys"

mkdir -p "/var/lib/spacewalk/pqkeys"
chown -R tomcat:susemanager "/var/lib/spacewalk/pqkeys"
chmod 0700 "/var/lib/spacewalk/pqkeys"
