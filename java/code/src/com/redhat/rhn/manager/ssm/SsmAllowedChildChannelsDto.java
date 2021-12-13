/*
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Dto holding allowed child channels for an SSM channel change.
 */
public class SsmAllowedChildChannelsDto {

    private Optional<SsmChannelDto> oldBaseChannel;
    private Optional<SsmChannelDto> newBaseChannel;
    private boolean newBaseDefault;
    private List<SsmServerDto> servers = new ArrayList<>();
    private List<SsmChannelDto> childChannels = new ArrayList<>();
    private List<SsmServerDto> incompatibleServers = new ArrayList<>();

    /**
     * Constructor
     * @param oldBaseChannelIn old base channel
     * @param newBaseChannelIn new base channel
     * @param newBaseDefaultIn if new base is a default channel
     */
    public SsmAllowedChildChannelsDto(Optional<SsmChannelDto> oldBaseChannelIn,
                                      Optional<SsmChannelDto> newBaseChannelIn,
                                      boolean newBaseDefaultIn) {
        this.oldBaseChannel = oldBaseChannelIn;
        this.newBaseChannel = newBaseChannelIn;
        this.newBaseDefault = newBaseDefaultIn;
    }

    /**
     * Constructor
     * @param oldBaseChannelIn old base channel
     * @param newBaseChannelIn new base channel
     * @param newBaseDefaultIn if new base is a default channel
     */
    public SsmAllowedChildChannelsDto(SsmChannelDto oldBaseChannelIn,
                                      SsmChannelDto newBaseChannelIn,
                                      boolean newBaseDefaultIn) {
        this(Optional.of(oldBaseChannelIn), Optional.of(newBaseChannelIn), newBaseDefaultIn);
    }

    /**
     * @return old base channel
     */
    public Optional<SsmChannelDto> getOldBaseChannel() {
        return oldBaseChannel;
    }

    /**
     * @return new base channel
     */
    public Optional<SsmChannelDto> getNewBaseChannel() {
        return newBaseChannel;
    }

    /**
     * @return servers
     */
    public List<SsmServerDto> getServers() {
        return servers;
    }

    /**
     * @param serversIn to set
     */
    public void setServers(List<SsmServerDto> serversIn) {
        this.servers = serversIn;
    }

    /**
     * @return child channels
     */
    public List<SsmChannelDto> getChildChannels() {
        return childChannels;
    }

    /**
     * @param childChannelsIn to set
     */
    public void setChildChannels(List<SsmChannelDto> childChannelsIn) {
        this.childChannels = childChannelsIn;
    }

    /**
     * @return incompatible servers
     */
    public List<SsmServerDto> getIncompatibleServers() {
        return incompatibleServers;
    }

    /**
     * If true the new base is a default system base.
     * @return newBaseDefault to get
     */
    public boolean isNewBaseDefault() {
        return newBaseDefault;
    }

    /**
     * @param newBaseDefaultIn to set
     */
    public void setNewBaseDefault(boolean newBaseDefaultIn) {
        this.newBaseDefault = newBaseDefaultIn;
    }

    /**
     * @param incompatibleServersIn to set
     */
    public void setIncompatibleServers(List<SsmServerDto> incompatibleServersIn) {
        this.incompatibleServers = incompatibleServersIn;
    }
}
