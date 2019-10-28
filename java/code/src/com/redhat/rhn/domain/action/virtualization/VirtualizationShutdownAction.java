/**
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

import java.util.Map;

/**
 * VirtualizationShutdownAction - Class representing TYPE_VIRTUALIZATION_SHUTDOWN
 * @version $Rev$
 */
public class VirtualizationShutdownAction extends BaseVirtualizationAction {

    private boolean force = false;

    /**
     * @return Returns whether to force off rather than cleanly shutting down.
     */
    public boolean isForce() {
        return force;
    }

    /**
     * @param forceIn whether to force off rather than cleanly shutting down.
     */
    public void setForce(boolean forceIn) {
        force = forceIn;
    }

    /**
     * {@inheritDoc}
     */
    public void extractParameters(Map context) {
        if (context.containsKey(BaseVirtualizationAction.FORCE_STRING)) {
            setForce(Boolean.valueOf((String)context.get(
                    BaseVirtualizationAction.FORCE_STRING)));
        }
    }
}
