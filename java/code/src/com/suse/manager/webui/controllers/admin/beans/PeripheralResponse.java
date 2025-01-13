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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PeripheralResponse {
    private long id;
    private String fqdn;
    private long nChannelsSync;
    private long nAllChannels;
    private long nOrgs;

    /**
     * Default constructor
     * @param idIn the id
     * @param fqdnIn the fully qualified domain name
     * @param nChannelsSyncIn the number of channels currently synced
     * @param nAllChannelsIn the total number of channels
     * @param nOrgsIn the number of organizations
     */
    public PeripheralResponse(long idIn, String fqdnIn, long nChannelsSyncIn, long nAllChannelsIn, long nOrgsIn) {
        this.id = idIn;
        this.fqdn = fqdnIn;
        this.nChannelsSync = nChannelsSyncIn;
        this.nAllChannels = nAllChannelsIn;
        this.nOrgs = nOrgsIn;
    }

    public long getId() {
        return id;
    }

    public void setId(long idIn) {
        this.id = idIn;
    }

    public String getFqdn() {
        return fqdn;
    }

    public void setFqdn(String fqdnIn) {
        this.fqdn = fqdnIn;
    }

    /**
     * gets the number of synced channels
     * @return the number of synced channel
     */
    public long getnChannelsSync() {
        return nChannelsSync;
    }

    /**
     * sets the number of synced channels
     * @param nChannelsSyncIn the number of synced channels
     */
    public void setnChannelsSync(long nChannelsSyncIn) {
        this.nChannelsSync = nChannelsSyncIn;
    }

    /**
     * gets the number of all channels
     * @return the number of all channels
     */
    public long getnAllChannels() {
        return nAllChannels;
    }

    /**
     * sets the number of all channels
     * @param nAllChannelsIn the number of all channels
     */
    public void setnAllChannels(long nAllChannelsIn) {
        this.nAllChannels = nAllChannelsIn;
    }

    /**
     * gets the number of organizations
     * @return the number of organizations
     */
    public long getnOrgs() {
        return nOrgs;
    }

    /**
     * sets the number of organizations
     * @param nOrgsIn the number of organizations
     */
    public void setnOrgs(long nOrgsIn) {
        this.nOrgs = nOrgsIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof PeripheralResponse that)) {
            return false;
        }

        return new EqualsBuilder()
            .append(getId(), that.getId())
            .append(getnChannelsSync(), that.getnChannelsSync())
            .append(getnAllChannels(), that.getnAllChannels())
            .append(getnOrgs(), that.getnOrgs())
            .append(getFqdn(), that.getFqdn())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getId())
            .append(getFqdn())
            .append(getnChannelsSync())
            .append(getnAllChannels())
            .append(getnOrgs())
            .toHashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PeripheralResponse{");
        sb.append("id=").append(id);
        sb.append(", fqdn='").append(fqdn).append('\'');
        sb.append(", nChannelsSync=").append(nChannelsSync);
        sb.append(", nAllChannels=").append(nAllChannels);
        sb.append(", nOrgs=").append(nOrgs);
        sb.append('}');
        return sb.toString();
    }
}
