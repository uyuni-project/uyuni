/*
 * Copyright (c) 2020 SUSE LLC
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
package com.redhat.rhn.domain.action.virtualization;

/**
 * Represents a virtual storage creation action.
 */
public class VirtualizationPoolCreateAction extends BaseVirtualizationPoolAction {

    private String uuid;
    private String type;
    private String target;
    private boolean autostart = false;

    // Permissions properties
    private String mode;
    private String owner;
    private String group;
    private String seclabel;

    private VirtualizationPoolCreateActionSource source;


    /**
     * @return Returns the uuid.
     */
    public String getUuid() {
        return uuid;
    }


    /**
     * @param uuidIn The uuid to set.
     */
    public void setUuid(String uuidIn) {
        uuid = uuidIn;
    }

    /**
     * @return Returns the pool type.
     */
    public String getType() {
        return type;
    }

    /**
     * @param typeIn The pool type to set.
     */
    public void setType(String typeIn) {
        type = typeIn;
    }

    /**
     * @return Returns the target.
     */
    public String getTarget() {
        return target;
    }

    /**
     * @param targetIn The target to set.
     */
    public void setTarget(String targetIn) {
        target = targetIn;
    }

    /**
     * @return Returns whether to autostart the pool.
     */
    public boolean isAutostart() {
        return autostart;
    }

    /**
     * @param autostartIn True if the pool needs to be automatically started.
     */
    public void setAutostart(boolean autostartIn) {
        autostart = autostartIn;
    }

    /**
     * @return Returns the permission mode.
     */
    public String getMode() {
        return mode;
    }

    /**
     * @param modeIn The permission mode to set.
     */
    public void setMode(String modeIn) {
        mode = modeIn;
    }

    /**
     * @return Returns the owner user.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @param ownerIn The owner user to set.
     */
    public void setOwner(String ownerIn) {
        owner = ownerIn;
    }


    /**
     * @return Returns the group.
     */
    public String getGroup() {
        return group;
    }

    /**
     * @param groupIn The group to set.
     */
    public void setGroup(String groupIn) {
        group = groupIn;
    }

    /**
     * @return Returns the SE Linux label.
     */
    public String getSeclabel() {
        return seclabel;
    }

    /**
     * @param seclabelIn The SE Linux label to set.
     */
    public void setSeclabel(String seclabelIn) {
        seclabel = seclabelIn;
    }

    /**
     * @return Returns the source.
     */
    public VirtualizationPoolCreateActionSource getSource() {
        return source;
    }

    /**
     * @return the source serialized into a String.
     *
     * This function should only be used by hibernate.
     */
    public String getSourceAsString() {
        String string = null;
        if (source != null) {
            string = source.toString();
        }
        return string;
    }

    /**
     * @param sourceIn The source to set.
     */
    public void setSource(VirtualizationPoolCreateActionSource sourceIn) {
        source = sourceIn;
    }

    /**
     * Set the source from its Serialized value.
     *
     * @param sourceString serialized value.
     *
     * This function should only be used by hibernate.
     */
    public void setSourceAsString(String sourceString) {
        if (sourceString != null) {
            source = VirtualizationPoolCreateActionSource.parse(sourceString);
        }
        else {
            source = null;
        }
    }

    @Override
    public String getWebSocketActionId() {
        String id = super.getWebSocketActionId();
        if (getUuid() == null) {
            id = String.format("new-%s", getId());
        }
        return id;
    }
}
