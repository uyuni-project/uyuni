#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-Only

mkdir -p /srv/www/distributions
chown tomcat:susemanager /srv/www/distributions
chmod 0775 /srv/www/distributions
