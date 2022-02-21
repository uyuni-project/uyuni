/*
 * Copyright (c) 2021 SUSE LLC
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

import com.redhat.rhn.frontend.dto.VirtualSystemOverview;

/**
 * Data exposed by the API for a virtual system
 */
public class VirtualSystem {

    private Long systemId;
    private String name;
    private String serverName;
    private String statusType;
    private String channelLabels;
    private Long channelId;
    private String uuid;
    private String stateName;
    private String stateLabel;
    private Long hostSystemId;
    private String hostServerName;
    private Long virtualSystemId;
    private Long vcpus;
    private Long memory;
    private boolean accessible;
    private boolean subscribable;

    /**
     * Constructor using a virtual system data from query result and the list of known virtual hosts.
     *
     * @param guestData the virtual guest data as returned by the SystemManager query
     */
    public VirtualSystem(VirtualSystemOverview guestData) {
        systemId = guestData.getSystemId();
        name = guestData.getName();
        serverName = guestData.getServerName();
        statusType = guestData.getStatusType();
        channelLabels = guestData.getChannelLabels();
        channelId = guestData.getChannelId();
        uuid = guestData.getUuid();
        stateName = guestData.getStateName();
        stateLabel = guestData.getStateLabel();
        hostSystemId = guestData.getHostSystemId();
        hostServerName = guestData.getHostServerName();
        virtualSystemId = guestData.getVirtualSystemId();
        vcpus = guestData.getVcpus();
        memory = guestData.getMemory();
        accessible = guestData.isAccessible();
        subscribable = guestData.isSubscribable();
    }


    /**
     * @return value of systemId
     */
    public Long getSystemId() {
        return systemId;
    }

    /**
     * @param systemIdIn value of systemId
     */
    public void setSystemId(Long systemIdIn) {
        systemId = systemIdIn;
    }

    /**
     * @return value of name
     */
    public String getName() {
        return name;
    }

    /**
     * @param nameIn value of name
     */
    public void setName(String nameIn) {
        name = nameIn;
    }

    /**
     * @return value of serverName
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * @param serverNameIn value of serverName
     */
    public void setServerName(String serverNameIn) {
        serverName = serverNameIn;
    }

    /**
     * @return value of statusType
     */
    public String getStatusType() {
        return statusType;
    }

    /**
     * @param statusTypeIn value of statusType
     */
    public void setStatusType(String statusTypeIn) {
        statusType = statusTypeIn;
    }

    /**
     * @return value of channelLabels
     */
    public String getChannelLabels() {
        return channelLabels;
    }

    /**
     * @param channelLabelsIn value of channelLabels
     */
    public void setChannelLabels(String channelLabelsIn) {
        channelLabels = channelLabelsIn;
    }

    /**
     * @return value of channelId
     */
    public Long getChannelId() {
        return channelId;
    }

    /**
     * @param channelIdIn value of channelId
     */
    public void setChannelId(Long channelIdIn) {
        channelId = channelIdIn;
    }

    /**
     * @return value of uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuidIn value of uuid
     */
    public void setUuid(String uuidIn) {
        uuid = uuidIn;
    }

    /**
     * @return value of stateName
     */
    public String getStateName() {
        return stateName;
    }

    /**
     * @param stateNameIn value of stateName
     */
    public void setStateName(String stateNameIn) {
        stateName = stateNameIn;
    }

    /**
     * @return value of stateLabel
     */
    public String getStateLabel() {
        return stateLabel;
    }

    /**
     * @param stateLabelIn value of stateLabel
     */
    public void setStateLabel(String stateLabelIn) {
        stateLabel = stateLabelIn;
    }

    /**
     * @return value of hostSystemId
     */
    public Long getHostSystemId() {
        return hostSystemId;
    }

    /**
     * @param hostSystemIdIn value of hostSystemId
     */
    public void setHostSystemId(Long hostSystemIdIn) {
        hostSystemId = hostSystemIdIn;
    }

    /**
     * @return value of hostServerName
     */
    public String getHostServerName() {
        return hostServerName;
    }

    /**
     * @param hostServerNameIn value of hostServerName
     */
    public void setHostServerName(String hostServerNameIn) {
        hostServerName = hostServerNameIn;
    }

    /**
     * @return value of virtualSystemId
     */
    public Long getVirtualSystemId() {
        return virtualSystemId;
    }

    /**
     * @param virtualSystemIdIn value of virtualSystemId
     */
    public void setVirtualSystemId(Long virtualSystemIdIn) {
        virtualSystemId = virtualSystemIdIn;
    }

    /**
     * @return value of vcpus
     */
    public Long getVcpus() {
        return vcpus;
    }

    /**
     * @param vcpusIn value of vcpus
     */
    public void setVcpus(Long vcpusIn) {
        vcpus = vcpusIn;
    }

    /**
     * @return value of memory
     */
    public Long getMemory() {
        return memory;
    }

    /**
     * @param memoryIn value of memory
     */
    public void setMemory(Long memoryIn) {
        memory = memoryIn;
    }

    /**
     * @return value of accessible
     */
    public boolean isAccessible() {
        return accessible;
    }

    /**
     * @param accessibleIn value of accessible
     */
    public void setAccessible(boolean accessibleIn) {
        accessible = accessibleIn;
    }

    /**
     * @return value of subscribable
     */
    public boolean isSubscribable() {
        return subscribable;
    }

    /**
     * @param subscribableIn value of subscribable
     */
    public void setSubscribable(boolean subscribableIn) {
        subscribable = subscribableIn;
    }
}
