/*
 * Copyright (c) 2017--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.webui.utils.gson;

import java.util.List;
import java.util.Optional;

/**
 * Subscribe to Channels JSON class
 */
public class SubscribeChannelsJson extends ScheduledRequestJson {

    private Optional<ChannelJson> base;
    private List<ChannelJson> children;

    /**
     * @return the base channel to set
     */
    public Optional<ChannelJson> getBase() {
        return base;
    }

    /**
     * @return the child channels to set
     */
    public List<ChannelJson> getChildren() {
        return children;
    }
}
