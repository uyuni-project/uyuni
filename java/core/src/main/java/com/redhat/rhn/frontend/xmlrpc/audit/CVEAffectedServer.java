/*
 * Copyright (c) 2023 SUSE LLC
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

package com.redhat.rhn.frontend.xmlrpc.audit;

import java.util.ArrayList;
import java.util.List;

/**
 * A server that is affected by a CVE
 * */
public class CVEAffectedServer {
    private Long id;
    private String name;
    private List<CVEAffectedPackage> affectedPackages;

    /**
     * Standard constructor
     * @param idIn the server id
     * @param nameIn the server name
     * @param affectedPackagesIn the list of affected packages
     */
    public CVEAffectedServer(Long idIn, String nameIn, List<CVEAffectedPackage> affectedPackagesIn) {
        this.id = idIn;
        this.name = nameIn;
        this.affectedPackages = new ArrayList<>(affectedPackagesIn);
    }

    public Long getId() {
        return id;
    }

    /**
     * Set the server id.
     * @param idIn the server id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    public String getName() {
        return name;
    }

    /**
     * Set the server name.
     * @param nameIn the server name
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    public List<CVEAffectedPackage> getAffectedPackages() {
        return affectedPackages;
    }

    /**
     * Set the list of affected packages.
     * @param affectedPackagesIn the list of affected packages
     */
    public void setAffectedPackages(List<CVEAffectedPackage> affectedPackagesIn) {
        this.affectedPackages = affectedPackagesIn;
    }
}
