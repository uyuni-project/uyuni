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
package com.redhat.rhn.frontend.dto;

import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.ServerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * DTO for empty system profile (salt minion with bootstrap entitlement).
 */
public class EmptySystemProfileOverview extends SystemOverview {

    /**
     * Gets the network interfaces' hardware addresses.
     * @return the MAC addresses
     */
    public List<String> getMacs() {
        List<String> result = new LinkedList<String>();
        for (NetworkInterface networkInterface : ServerFactory.lookupById(getId())
                .getNetworkInterfaces()) {
            if (networkInterface.isMacValid()) {
                result.add(networkInterface.getHwaddr());
            }
        }
        return result;
    }
}
