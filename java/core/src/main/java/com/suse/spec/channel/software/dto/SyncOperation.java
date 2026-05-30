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
package com.suse.spec.channel.software.dto;

/**
 * Type of sync operation to perform.
 */
public enum SyncOperation {
    /**
     * Sync only erratas, no packages.
     */
    ERRATA_ONLY,

    /**
     * Sync only packages, no erratas.
     */
    PACKAGES_ONLY,

    /**
     * Sync both erratas and packages.
     */
    ERRATA_AND_PACKAGES;

    /**
     * Checks if this sync operation includes erratas.
     *
     * @return true if this operation includes erratas
     */
    public boolean includesErratas() {
        return this == ERRATA_ONLY || this == ERRATA_AND_PACKAGES;
    }

    /**
     * Checks if this sync operation includes packages.
     *
     * @return true if this operation includes packages
     */
    public boolean includesPackages() {
        return this == PACKAGES_ONLY || this == ERRATA_AND_PACKAGES;
    }
}
