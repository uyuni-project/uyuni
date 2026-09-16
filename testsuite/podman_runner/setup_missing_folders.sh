#!/usr/bin/bash

# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: MIT

for folder in systems packages; do
    mkdir -p /var/spacewalk/${folder}
    chmod 0775 /var/spacewalk/${folder}
    chown wwwrun:www /var/spacewalk/${folder}
done

mkdir -p /var/spacewalk/gpg
chmod 0700 /var/spacewalk/gpg
chown tomcat:tomcat /var/spacewalk/gpg
