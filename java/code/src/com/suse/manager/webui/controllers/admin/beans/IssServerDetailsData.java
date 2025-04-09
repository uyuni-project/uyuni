/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.webui.controllers.admin.beans;

import com.suse.manager.model.hub.IssRole;

import java.util.Date;

public abstract class IssServerDetailsData {

    private final long id;
    private final IssRole role;
    private final String fqdn;
    private final String rootCA;
    private final String sccUsername;
    private final Date created;
    private final Date modified;

    protected IssServerDetailsData(long idIn, IssRole roleIn, String fqdnIn, String rootCAIn, String sccUsernameIn,
                                   Date createdIn, Date modifiedIn) {
        this.id = idIn;
        this.role = roleIn;
        this.fqdn = fqdnIn;
        this.rootCA = rootCAIn;
        this.sccUsername = sccUsernameIn;
        this.created = createdIn;
        this.modified = modifiedIn;
    }

    public long getId() {
        return id;
    }

    public IssRole getRole() {
        return role;
    }

    public String getFqdn() {
        return fqdn;
    }

    public String getRootCA() {
        return rootCA;
    }

    public String getSccUsername() {
        return sccUsername;
    }

    public Date getCreated() {
        return created;
    }

    public Date getModified() {
        return modified;
    }
}
