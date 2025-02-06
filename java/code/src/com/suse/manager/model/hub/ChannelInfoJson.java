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

package com.suse.manager.model.hub;

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
}
