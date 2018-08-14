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
package com.suse.manager.webui.utils.gson;

import java.util.Set;

/**
 * JSON representation of a server and a list of Salty config channels.
 */
public class ServerConfigChannelsJson {

    /** Server id */
    private long id;

    private StateTargetType type;

    private Set<ConfigChannelJson> channels;

    /**
     * @return the sever id
     */
    public Long getTargetId() {
        return id;
    }

    /**
     * @return the target type
     */
    public StateTargetType getTargetType() {
        return type;
    }

    /**
     * @return the custom Salt states
     */
    public Set<ConfigChannelJson> getChannels() {
        return channels;
    }


}
