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

package com.redhat.rhn.domain.server;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import java.util.stream.Stream;

/**
 * Factory class for the {@link com.redhat.rhn.domain.server.NetworkInterface}
 */
public class NetworkInterfaceFactory {

    /**
     * Constructor preventing instantiation.
     */
    private NetworkInterfaceFactory() { }

    /**
     * Looks up network interfaces based on given hardware address (MAC)
     * @param hwAddress the hardware address
     * @return stream of network interfaces having the given hardware address
     */
    public static Stream<NetworkInterface> lookupNetworkInterfacesByHwAddress(String hwAddress) {
        return HibernateFactory.getSession()
                .getNamedQuery("NetworkInterface.lookupByHwAddress")
                .setParameter("hwAddress", hwAddress)
                .stream();
    }

}
