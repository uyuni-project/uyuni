/*
 * Copyright (c) 2017 SUSE LLC
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

import com.redhat.rhn.manager.ssm.SsmChannelDto;
import com.redhat.rhn.manager.ssm.SsmServerDto;

import java.util.List;

/**
 * Dto object holding the base channels that
 * are allowed to change to in SSM.
 */
public class SsmAllowedBaseChannelsJson {

    private SsmChannelDto base;
    private List<SsmChannelDto> allowedBaseChannels;
    private List<SsmServerDto> servers;

    /**
     * @return get base channel
     */
    public SsmChannelDto getBase() {
        return base;
    }

    /**
     * @param baseIn to set
     */
    public void setBase(SsmChannelDto baseIn) {
        this.base = baseIn;
    }

    /**
     * @param serversIn to set
     */
    public void setServers(List<SsmServerDto> serversIn) {
        this.servers = serversIn;
    }

    /**
     * @return servers subscribed to the current base channel
     */
    public List<SsmServerDto> getServers() {
        return servers;
    }

    /**
     * @return the allowed base channels
     */
    public List<SsmChannelDto> getAllowedBaseChannels() {
        return allowedBaseChannels;
    }

    /**
     * @param allowedBaseChannelsIn to set
     */
    public void setAllowedBaseChannels(List<SsmChannelDto> allowedBaseChannelsIn) {
        this.allowedBaseChannels = allowedBaseChannelsIn;
    }
}
