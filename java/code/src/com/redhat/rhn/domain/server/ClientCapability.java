/**
 * Copyright (c) 2016 SUSE LLC
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
package com.redhat.rhn.domain.server;

import com.redhat.rhn.domain.BaseDomainHelper;

/**
 * ClientCapability
 * @version $Rev$
 */
public class ClientCapability extends BaseDomainHelper {

    private ClientCapabilityId id;

    private long version;

    /**
     * No arg constructor needed by Hibernate.
     */
    public ClientCapability() {
    }

    /**
     * @param server the server
     * @param capability the capability
     * @param versionIn the version
     */
    public ClientCapability(Server server, Capability capability, long versionIn) {
        this.id = new ClientCapabilityId(server, capability);
        this.version = versionIn;
    }

    /**
     * @return the id
     */
    public ClientCapabilityId getId() {
        return id;
    }

    /**
     * @param idIn the id
     */
    public void setId(ClientCapabilityId idIn) {
        this.id = idIn;
    }

    /**
     * @return the version
     */
    public long getVersion() {
        return version;
    }

    /**
     * @param versionIn set the version
     */
    public void setVersion(long versionIn) {
        this.version = versionIn;
    }
}
