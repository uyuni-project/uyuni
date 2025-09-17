/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
import com.redhat.rhn.domain.action.kickstart.KickstartGuestAction;
import com.redhat.rhn.domain.action.kickstart.KickstartGuestActionDetails;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cobbler.Network;
import org.cobbler.SystemRecord;
import org.cobbler.XmlRpcException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


/**
 *
 */
public class CobblerVirtualSystemCommand extends CobblerSystemCreateCommand {

    /**
     * Logger for this class
     */
    private static Logger log = LogManager.getLogger(CobblerVirtualSystemCommand.class);

    private String guestName;
    private String hostName;

    /**
     * Constructor
     * @param userIn who is requesting this sync
     * @param serverIn to create in cobbler
     * @param cobblerProfileName to use
     * @param guestNameIn the guest name to create
     * @param ksData the kickstart data to associate
     *      system with
     */
    public CobblerVirtualSystemCommand(User userIn, Server serverIn,
            String cobblerProfileName, String guestNameIn, KickstartData ksData) {
        super(userIn, serverIn, cobblerProfileName, ksData);
        guestName = guestNameIn;
        hostName = serverIn.getName();
    }

    /**
     * Constructor for VMs on Salt Virtual hosts
     *
     * @param userIn the user creating the VM
     * @param cobblerProfileName to use
     * @param guestNameIn the guest name to create
     * @param ksData the kickstart data to associate system with
     * @param hostNameIn the name of the virtual host system
     * @param orgId the organization ID the system will belong to
     */
    public CobblerVirtualSystemCommand(User userIn, String cobblerProfileName, String guestNameIn, KickstartData ksData,
                                       String hostNameIn, Long orgId) {
        super(userIn, cobblerProfileName, ksData, guestNameIn, orgId);
        guestName = guestNameIn;
        hostName = hostNameIn;
    }

    /**
     * Constructor
     * @param userIn who is requesting the sync
     * @param serverIn profile we want to create in cobbler
     * @param ksDataIn profile to associate with with server.
     * @param mediaPathIn mediaPath to override in the server profile.
     * @param activationKeysIn to add to the system record.  Used when the system
     * @param guestNameIn the guest name to create
     * re-registers to Spacewalk
     */
    public CobblerVirtualSystemCommand(User userIn, Server serverIn,
            KickstartData ksDataIn, String mediaPathIn,
            String activationKeysIn, String guestNameIn) {
        super(userIn, serverIn, ksDataIn, mediaPathIn, activationKeysIn);
        guestName = guestNameIn;
        hostName = serverIn.getName();
    }

    /**
     * Constructor
     * @param userIn who is requesting the sync
     * @param serverIn profile we want to create in cobbler
     * @param nameIn profile nameIn to associate with with server.
     */
    public CobblerVirtualSystemCommand(User userIn, Server serverIn,
            String nameIn) {
        super(userIn, serverIn, nameIn);
        hostName = serverIn.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCobblerSystemRecordName() {
        String sep = ConfigDefaults.get().getCobblerNameSeparator();
        return CobblerSystemCreateCommand.getCobblerSystemRecordName(hostName, getOrgId()) + sep +
                guestName.replace(' ', '_').replaceAll("[^a-zA-Z0-9_\\-\\.]", "");
    }

    @Override
    protected void processNetworkInterfaces(SystemRecord rec, Server serverIn) {
        log.debug("processNetworkInterfaces called.");

        KickstartGuestAction action = (KickstartGuestAction) getScheduledAction();
        // When creation VMs using Salt we don't care about the interfaces
        if (action != null) {
            KickstartGuestActionDetails details = action
                    .getKickstartGuestActionDetails();
            String newMac = details.getMacAddress();
            if (newMac == null || newMac.equals("")) {
                newMac = (String) invokeXMLRPC("get_random_mac",
                        Collections.emptyList());
            }
            Network net = new Network(getCobblerConnection(), "eth0");
            net.setMacAddress(newMac);
            List<Network> nics = new LinkedList<>();
            nics.add(net);
            rec.setNetworkInterfaces(nics);
        }
    }


    @Override
    protected SystemRecord lookupExisting(Server server) {
        log.debug("lookupExisting called.");

        return SystemRecord.lookupByName(
                CobblerXMLRPCHelper.getConnection(user), getCobblerSystemRecordName());
    }


    /**
     * Updates the cobbler virt attributes based on
     * params provided
     * @param memoryMB the memory in MB
     * @param diskSizeGb the diskSize in GB
     * @param vcpus the number of cpus
     * @param diskPath the disk path of the virt image.
     */
    protected void setupVirtAttributes(int memoryMB, int diskSizeGb,
            int vcpus, String diskPath) {
        SystemRecord rec = SystemRecord.lookupByName(
                CobblerXMLRPCHelper.getConnection(user), getCobblerSystemRecordName());
        if (rec != null) {
            rec.setVirtRam(Optional.of(memoryMB));
            rec.setVirtFileSize(Optional.of(diskSizeGb));
            rec.setVirtCpus(Optional.of(vcpus));
            rec.setVirtPath(Optional.of(diskPath));
            rec.save();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidatorError store() {
        ValidatorError error = null;
        try {
            error = super.store(false);
        }
        catch (XmlRpcException e) {
            if (e.getCause() != null &&
                    e.getCause().getMessage() != null &&
                    e.getCause().getMessage()
                    .contains("MAC address duplicated")) {
                error = new ValidatorError(
                        "frontend.actions.systems.virt.duplicatemacaddressvalue");
            }
            else {
                throw e;
            }
        }
        if (error == null) {
            KickstartGuestAction action = (KickstartGuestAction) getScheduledAction();
            // When creating a VM on a Salt minion we don't handle the virt attributes
            if (action != null) {
                KickstartGuestActionDetails details = action.getKickstartGuestActionDetails();
                setupVirtAttributes(details.getMemMb().intValue(),
                        details.getDiskGb().intValue(),
                        details.getVcpus().intValue(),
                        details.getDiskPath());
            }
        }
        return error;
    }

}
