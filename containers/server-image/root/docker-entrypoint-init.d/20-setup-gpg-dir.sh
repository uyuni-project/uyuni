#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-Only

mkdir -p "/var/spacewalk/gpg"
chown -R tomcat:tomcat "/var/spacewalk/gpg"
chmod 0700 "/var/spacewalk/gpg"

mkdir -p "/var/lib/spacewalk/gpgdir"
chown -R tomcat:tomcat "/var/lib/spacewalk/gpgdir"
chmod 0700 "/var/lib/spacewalk/gpgdir"
