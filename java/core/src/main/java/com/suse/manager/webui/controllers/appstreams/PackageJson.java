/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.controllers.appstreams;

import com.redhat.rhn.domain.rhnpackage.Package;

/**
 * Represents a JSON object for AppStream package entries
 */
public class PackageJson {
    /**
     * Instantiate a new JSON object
     * @param packageIn the package
     */
    public PackageJson(Package packageIn) {
        this(packageIn.getId(), packageIn.getNevraWithEpoch());
    }

    /**
     * Instantiate a JSON object
     * @param idIn the package ID
     * @param nevraIn the package NEVRA
     */
    public PackageJson(Long idIn, String nevraIn) {
        nevra = nevraIn;
        id = idIn;
    }

    private final String nevra;
    private final Long id;

    public String getNevra() {
        return nevra;
    }

    public Long getId() {
        return id;
    }
}
