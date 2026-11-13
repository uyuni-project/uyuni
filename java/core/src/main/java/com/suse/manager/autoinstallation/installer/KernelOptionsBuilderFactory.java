/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.autoinstallation.installer;

import com.redhat.rhn.domain.kickstart.KickstartInstallType;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.autoinstallation.builder.AgamaKernelOptionsBuilder;
import com.suse.manager.autoinstallation.builder.AutoYastKernelOptionsBuilder;
import com.suse.manager.autoinstallation.builder.KernelOptionsBuilder;
import com.suse.manager.autoinstallation.builder.KernelOptionsBuilderException;
import com.suse.manager.autoinstallation.builder.RhelKernelOptionsBuilder;

/**
 * Factory routing install types to their kernel options builder. The
 * install type is the single routing key: SLE16 is checked before the SUSE
 * branch because opensuse16.1 satisfies isSUSE() as well, and unknown types
 * are rejected instead of defaulted.
 */
public class KernelOptionsBuilderFactory {

    private KernelOptionsBuilderFactory() {
    }

    /**
     * Returns the builder for an install type without a user context
     * (global installer-updates child channel lookup).
     *
     * @param installType the install type to route by
     * @param serverFqdn the kickstart host
     * @return the matching builder
     * @throws KernelOptionsBuilderException if the install type or the
     *         server FQDN is null/empty or has no matching builder
     */
    public static KernelOptionsBuilder getBuilder(KickstartInstallType installType, String serverFqdn) {
        return getBuilder(installType, serverFqdn, null);
    }

    /**
     * Returns the builder for an install type with a user context
     * (user-scoped installer-updates child channel lookup).
     *
     * @param installType the install type to route by
     * @param serverFqdn the kickstart host
     * @param user the user context, null for org-global lookup
     * @return the matching builder
     * @throws KernelOptionsBuilderException if the install type or the
     *         server FQDN is null/empty or has no matching builder
     */
    public static KernelOptionsBuilder getBuilder(KickstartInstallType installType, String serverFqdn, User user) {
        if (serverFqdn == null || serverFqdn.isEmpty()) {
            throw new KernelOptionsBuilderException("Server FQDN must not be empty");
        }
        if (installType == null) {
            throw new KernelOptionsBuilderException("Install type must not be null");
        }
        if (installType.isSLES16OrGreater()) {
            return new AgamaKernelOptionsBuilder(serverFqdn, user);
        }
        if (installType.isSUSE()) {
            return new AutoYastKernelOptionsBuilder(serverFqdn, user);
        }
        if (installType.isRhel() || installType.isFedora() || installType.isGeneric()) {
            return new RhelKernelOptionsBuilder(serverFqdn, user);
        }
        throw new KernelOptionsBuilderException(
                "No kernel options builder for install type: " + installType.getLabel());
    }
}
