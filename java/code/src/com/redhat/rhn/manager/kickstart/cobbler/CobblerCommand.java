/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cobbler.CobblerConnection;
import org.cobbler.Network;
import org.cobbler.SystemRecord;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


/**
 * CobblerCommand - class to contain logic to communicate with cobbler
 */
public abstract class CobblerCommand {

    private static final Logger LOG = LogManager.getLogger(CobblerCommand.class);

    protected User user;
    protected final CobblerConnection cobblerConnection;


    /**
     * Construct a CobblerCommand
     * @param userIn - xmlrpc token for cobbler
     */
    protected CobblerCommand(User userIn) {
        user = userIn;
        if (user == null) {
            cobblerConnection = CobblerXMLRPCHelper.getAutomatedConnection();
            LOG.debug("Cobbler XML-RPC session created for taskomatic_user");
        }
        else {
            cobblerConnection = CobblerXMLRPCHelper.getConnection(userIn);
            LOG.debug("Cobbler XML-RPC session created for user \"{}\"", user.getId());
        }
    }

    /**
     * Construct a CobblerCommand without using authentication
     *  This should only be used for taskomatic!
     */
    protected CobblerCommand() {
        this(null);
    }

    /**
     * Sync the KickstartData to the Cobbler object
     *
     * @return ValidatorError if there is any errors
     */
    public abstract ValidatorError store();

    /**
     * Makes a simple profile or distro object
     * name that 'd fit our cobbler naming convention
     * @param label the distro or profile label
     * @param org the org to appropriately add the org info
     * @return the cobbler name.
     */
    public static String makeCobblerName(String label, Org org) {
        String sep = ConfigDefaults.get().getCobblerNameSeparator();
        label = label.replace(' ', '_').replaceAll("[^a-zA-Z0-9_-]", "");

        if (org == null) {
            return label;
        }

        String orgName = org.getName().replaceAll("[^a-zA-Z0-9_-]", "").replace(' ', '_');
        String format = "%s" + sep + "%s" + sep + "%s";
        return String.format(format, label, org.getId(), orgName);

    }

    /**
     * Makes a local file path out of the cobbler name.
     * <br>
     * Currently ends up in: {@code /var/lib/rhn/kickstarts/label--orgid--orgname.cfg}
     *
     * @param label the distro or profile label
     * @param org the org to appropriately add the org info
     * @return the cobbler file name in {@code /var/lib/rhn/kickstarts/label--orgid--orgname.cfg}
     */
    public static String makeCobblerFileName(String label, Org org) {
        if (org == null) {
            return label.replace(' ', '_');
        }
        String format = "%s--%s";
        String kickstartConfigDir = ConfigDefaults.get().getKickstartConfigDir();
        String fileName = String.format(format, label.replace(' ', '_'), org.getId());
        return kickstartConfigDir + fileName + ".cfg";
    }

    /**
     * Make a cobbler name for a kickstartable tree
     * @param tree the tree
     * @return the name
     */
    public static String makeCobblerName(KickstartableTree tree) {
        return makeCobblerName(tree.getLabel(), tree.getOrg());
    }

    /**
     * Make a cobbler name for a kickstart profile
     * @param data the profile
     * @return the name
     */
    public static String makeCobblerName(KickstartData data) {
        return makeCobblerName(data.getLabel(), data.getOrg());
    }

    protected CobblerConnection getCobblerConnection() {
        return getCobblerConnection(user);
    }

    protected static CobblerConnection getCobblerConnection(User user) {
        if (user == null) {
            return CobblerXMLRPCHelper.getAutomatedConnection();
        }
        return CobblerXMLRPCHelper.getConnection(user);
    }

    /**
     * Lookup system record 1st with id and then by mac
     * @param server server
     * @return system record
     */
    protected SystemRecord lookupExisting(Server server) {
        if (server.getCobblerId() != null) {
            SystemRecord rec = SystemRecord.lookupById(cobblerConnection, server.getCobblerId());
            if (rec != null) {
                return rec;
            }
        }
        //lookup by ID failed, so lets try by mac

        SystemRecord system = getSystemByMac(server);
        if (system != null) {
            LOG.debug("getSystemHandleByMAC.found match.");
            if (!Objects.equals(system.getId(), "")) {
                return system;
            }
        }
        return null;
    }
    /**
     * Get system map by mac address
     * @return system Map
     * @param server server
     */
    protected SystemRecord getSystemByMac(Server server) {
        // Build up list of mac addrs
        List<String> macs = new LinkedList<>();
        for (NetworkInterface n : server.getNetworkInterfaces()) {
            // Skip localhost and non-real interfaces
            if (!n.isMacValid()) {
                LOG.debug("Interface \"{}\" not a real interface. Skipping!", n.getName());
            }
            else {
                macs.add(n.getHwaddr().toLowerCase());
            }

        }

        List<SystemRecord> systems = SystemRecord.list(cobblerConnection);
        for (SystemRecord row : systems) {
            List<Network> ifacenames = row.getNetworkInterfaces();
            LOG.debug("Found \"{}\" interfaces in Cobbler System \"{}\"", ifacenames.size(), row.getId());
            for (Network networkInterface : ifacenames) {
                if (networkInterface.getMacAddress() != null &&
                        macs.contains(networkInterface.getMacAddress().toLowerCase())) {
                    LOG.debug("getSystemByMac found match for interface \"{}\".", networkInterface.getName());
                    return row;
                }
            }
        }
        return null;
    }
}
