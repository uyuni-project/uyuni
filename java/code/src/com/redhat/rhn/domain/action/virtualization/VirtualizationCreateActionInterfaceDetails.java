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
package com.redhat.rhn.domain.action.virtualization;

import java.util.Objects;

/**
 * Represents the network interface configuration options when creating a Virtual machine
 */
public class VirtualizationCreateActionInterfaceDetails {

    private Long id;
    private VirtualizationCreateAction action;
    private String type;
    private String source;
    private String mac;

    /**
     * @return the network details ID in the DB
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn the network details ID in the DB
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * @return the associated virtualization create action details
     */
    public VirtualizationCreateAction getAction() {
        return action;
    }

    /**
     * @param actionIn the associated virtualization create action details
     */
    public void setAction(VirtualizationCreateAction actionIn) {
        action = actionIn;
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }

    /**
     * @param typeIn The type to set.
     */
    public void setType(String typeIn) {
        type = typeIn;
    }

    /**
     * @return the source to use for the network
     */
    public String getSource() {
        return source;
    }

    /**
     * @param sourceIn the source to use for the network
     */
    public void setSource(String sourceIn) {
        source = sourceIn;
    }

    /**
     * @return Returns the mac.
     */
    public String getMac() {
        return mac;
    }

    /**
     * @param macIn The mac to set.
     */
    public void setMac(String macIn) {
        mac = macIn;
    }

    @Override
    public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof VirtualizationCreateActionInterfaceDetails) {
            VirtualizationCreateActionInterfaceDetails otherNic = (VirtualizationCreateActionInterfaceDetails)other;
            result = Objects.equals(getType(), otherNic.getType()) &&
                    Objects.equals(getSource(), otherNic.getSource()) &&
                    Objects.equals(getMac(), otherNic.getMac());
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, source, mac);
    }
}
