#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

# Keeps the Kiwi OS-image trusted-CA RPM in sync with the server CA bundle.

ca_file="/etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT"
rpm_file="/srv/susemanager/salt/images/rhn-org-trusted-ssl-cert-osimage-1.0-1.noarch.rpm"
packaged_name="/usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT"

# Nothing to package without a CA file.
[ -f "$ca_file" ] || exit 0

needs_regen() {
    [ -f "$rpm_file" ] || return 0

    local algo sum_cmd current packaged

    # Find the checksum type of the RPM and use the corresponding utility.
    algo=$(rpm -qp --qf '%{FILEDIGESTALGO}\n' "$rpm_file" 2>/dev/null)
    case "$algo" in
        2)  sum_cmd=sha1sum ;;
        8)  sum_cmd=sha256sum ;;
        9)  sum_cmd=sha384sum ;;
        10) sum_cmd=sha512sum ;;
        11) sum_cmd=sha224sum ;;
        *)  sum_cmd=md5sum ;;
    esac

    current=$("$sum_cmd" "$ca_file" 2>/dev/null | cut -d' ' -f1)
    packaged=$(rpm -qp --qf "[%{FILENAMES} %{FILEDIGESTS}\n]" "$rpm_file" 2>/dev/null \
        | sed -ne "s@${packaged_name} \(.*\)\$@\1@p")

    [ -z "$packaged" ] || [ "$current" != "$packaged" ]
}

if needs_regen; then
    echo "Server CA changed; regenerating the OS image trusted CA RPM for Kiwi builds..."
    if ! /usr/sbin/mgr-package-rpm-certificate-osimage; then
        echo "WARNING: failed to regenerate $rpm_file! Kiwi images may not trust the current CA." >&2
    fi
fi

exit 0
