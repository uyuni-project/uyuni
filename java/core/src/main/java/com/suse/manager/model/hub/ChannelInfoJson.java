/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.model.hub;

import java.util.Objects;

public class ChannelInfoJson {

    private final Long id;
    private final String name;
    private final String label;
    private final String summary;
    private final Long orgId;
    private final Long parentChannelId;

    /**
     * Constructor
     *
     * @param idIn              the id of the channel, if present
     * @param nameIn            the name of the channel
     * @param labelIn           the label of the channel
     * @param summaryIn         the summary of the channel
     * @param orgIdIn           the organization id of the channel
     * @param parentChannelIdIn the parent channel of the channel
     */
    public ChannelInfoJson(Long idIn, String nameIn, String labelIn, String summaryIn,
                           Long orgIdIn, Long parentChannelIdIn) {
        this.id = idIn;
        this.name = nameIn;
        this.label = labelIn;
        this.summary = summaryIn;
        this.orgId = orgIdIn;
        this.parentChannelId = parentChannelIdIn;
    }

    /**
     * @return the id of the channel
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the label of the channel
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return the name of the channel
     */
    public String getName() {
        return name;
    }

    /**
     * @return the summary of the channel
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @return the organization id of the channel
     */
    public Long getOrgId() {
        return orgId;
    }

    /**
     * @return the parent channel of the channel
     */
    public Long getParentChannelId() {
        return parentChannelId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChannelInfoJson that)) {
            return false;
        }
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getLabel(), that.getLabel()) &&
                Objects.equals(getSummary(), that.getSummary()) &&
                Objects.equals(getOrgId(), that.getOrgId()) &&
                Objects.equals(getParentChannelId(), that.getParentChannelId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getLabel(), getSummary(), getOrgId(), getParentChannelId());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChannelInfoJson{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", label='").append(label).append('\'');
        sb.append(", summary='").append(summary).append('\'');
        sb.append(", orgId=").append(orgId);
        sb.append(", parentChannelId=").append(parentChannelId);
        sb.append('}');
        return sb.toString();
    }
}

