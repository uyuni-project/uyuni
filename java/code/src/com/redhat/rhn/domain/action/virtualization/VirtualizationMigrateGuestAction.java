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
package com.redhat.rhn.domain.action.virtualization;

/**
 * Represents a guest migration action
 */
public class VirtualizationMigrateGuestAction extends BaseVirtualizationGuestAction {
    public static final String PRIMITIVE = "primitive";
    public static final String TARGET = "target";

    private String primitive;
    private String target;

    /**
     * @return value of primitive
     */
    public String getPrimitive() {
        return primitive;
    }

    /**
     * @param primitiveIn value of primitive
     */
    public void setPrimitive(String primitiveIn) {
        primitive = primitiveIn;
    }

    /**
     * @return value of target
     */
    public String getTarget() {
        return target;
    }

    /**
     * @param targetIn value of target
     */
    public void setTarget(String targetIn) {
        target = targetIn;
    }
}
