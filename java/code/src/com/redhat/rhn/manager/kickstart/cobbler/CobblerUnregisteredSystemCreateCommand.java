/**
 * Copyright (c) 2011--2012 Novell
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
package com.redhat.rhn.manager.kickstart.cobbler;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.cobbler.Network;
import org.cobbler.SystemRecord;

import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;

/**
 * Create a cobbler system record for a bare metal system (via XMLRPC) that is
 * not (yet) registered with spacewalk.
 *
 * @version $Rev$
 */
public class CobblerUnregisteredSystemCreateCommand extends
        CobblerSystemCreateCommand {

    /**
     * Constructor
     *
     * @param userIn user
     * @param serverIn server
     * @param nameIn name
     */
    public CobblerUnregisteredSystemCreateCommand(User userIn, Server serverIn,
            String nameIn) {
        super(userIn, serverIn, nameIn);
    }

    /**
     * Accept an interface as soon as it has a name and a valid MAC or IP
     * address.
     */
    @Override
    protected void processNetworkInterfaces(SystemRecord rec, Server serverIn) {
        List<Network> ifaces = new LinkedList<Network>();
        if (serverIn.getNetworkInterfaces() != null) {
            for (NetworkInterface n : serverIn.getNetworkInterfaces()) {
                Network net = new Network(getCobblerConnection(), n.getName());
                net.setIpAddress(n.getIpaddr());
                net.setMacAddress(n.getHwaddr());
                net.setNetmask(n.getNetmask());
                if (!StringUtils.isBlank(networkInterface) &&
                        n.getName().equals(networkInterface)) {
                    net.setStaticNetwork(!isDhcp);
                }
                ifaces.add(net);
            }
        }
        rec.setNetworkInterfaces(ifaces);
    }

    /**
     * Define this class here since it should only be used to passing on data to
     * cobbler.
     */
    public class CobblerNetworkInterface extends NetworkInterface {

        private static final long serialVersionUID = -8764861534739929941L;
        private String ipAddress;

        /**
         * Allow to set the IP directly for passing it on to cobbler.
         * @param ip ip address
         */
        public void setIpaddr(String ip) {
            this.ipAddress = ip;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getIpaddr() {
            return this.ipAddress;
        }
    }
}
