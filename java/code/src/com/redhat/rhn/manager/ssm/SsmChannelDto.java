/**
 * Copyright (c) 2018 SUSE LLC
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

package com.redhat.rhn.manager.ssm;

import com.redhat.rhn.domain.channel.Channel;

/**
 * Dto holding basic channel info.
 */
public class SsmChannelDto {

    private long id;
    private String name;
    private boolean custom;
    private boolean recommended;

    /**
     * Constructor.
     * @param idIn channel id
     * @param nameIn channel name
     * @param customIn if channel is custom or not
     */
    public SsmChannelDto(long idIn, String nameIn, boolean customIn) {
        this.id = idIn;
        this.name = nameIn;
        this.custom = customIn;
        this.recommended = false;
    }

    /**
     * @return channel id
     */
    public long getId() {
        return id;
    }

    /**
     * @return channel name
     */
    public String getName() {
        return name;
    }

    /**
     * @return channel custom flag
     */
    public boolean isCustom() {
        return custom;
    }

    /**
     * Gets the recommended flag.
     *
     * @return the recommended flag
     */
    public boolean isRecommended() {
        return recommended;
    }

    /**
     * Sets the recommended flag.
     *
     * @param recommendedIn - the recommended flag
     */
    public void setRecommended(boolean recommendedIn) {
        recommended = recommendedIn;
    }

    /**
     * Factory method for creating dto from {@link Channel} obj.
     * @param channel a chanel obj
     * @return a dto
     */
    public static SsmChannelDto from(Channel channel) {
        return new SsmChannelDto(channel.getId(), channel.getName(), channel.isCustom());
    }
}
