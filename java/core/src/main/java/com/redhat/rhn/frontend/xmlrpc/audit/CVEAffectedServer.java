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

    public CVEAffectedServer(Long id, String name, List<CVEAffectedPackage> affectedPackages) {
        this.id = id;
        this.name = name;
        this.affectedPackages = new ArrayList<>(affectedPackages);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CVEAffectedPackage> getAffectedPackages() {
        return affectedPackages;
    }

    public void setAffectedPackages(List<CVEAffectedPackage> affectedPackages) {
        this.affectedPackages = affectedPackages;
    }
}
