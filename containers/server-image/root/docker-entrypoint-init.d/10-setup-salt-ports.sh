#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

# Set the Salt ports the server is exposed on, for example with Kubernetes NodePorts,
# from the MANAGER_SALT_PUBLISH_PORT and MANAGER_SALT_REQUEST_PORT environment variables.
# An empty variable resets to the Salt default, an unset one keeps rhn.conf untouched.
set_salt_port() {
    local key="$1"
    local value="$2"
    sed -i "/^${key}\s*=/d" /etc/rhn/rhn.conf
    if [ -n "${value}" ]; then
        echo "${key} = ${value}" >> /etc/rhn/rhn.conf
    fi
}

if [ -n "${MANAGER_SALT_PUBLISH_PORT+x}" ]; then
    set_salt_port java.salt_publish_port "${MANAGER_SALT_PUBLISH_PORT}"
fi
if [ -n "${MANAGER_SALT_REQUEST_PORT+x}" ]; then
    set_salt_port java.salt_request_port "${MANAGER_SALT_REQUEST_PORT}"
fi
