/*
 * Copyright (c) 2013 SUSE LLC
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
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerHistoryEvent;
import com.redhat.rhn.domain.user.User;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cobbler.CobblerConnection;
import org.cobbler.SystemRecord;
import org.cobbler.XmlRpcException;

import java.util.Date;

/**
 * Powers on a system.
 */
public class CobblerPowerCommand extends CobblerCommand {

    /** The log. */
    private static Logger log = LogManager.getLogger(CobblerPowerCommand.class);

    /** The server to power on or off. */
    private Server server;

    /** Alternative the cobbler system prefix */
    private String name;

    /** Power management operation kind. */
    private Operation operation;

    /**
     * Possible power management operations.
     */
    public enum Operation {
        /** Turn on. */
        PowerOn,
        /** Turn off. */
        PowerOff,
        /** Reboot. */
        Reboot
    }

    /**
     * Instantiates a new Cobbler power management command.
     * @param userIn the user running this command
     * @param serverIn the server to power on or off
     * @param operationIn the operation to run
     */
    public CobblerPowerCommand(User userIn, Server serverIn, Operation operationIn) {
        super(userIn);
        server = serverIn;
        operation = operationIn;
    }

    /**
     * Instantiates a new Cobbler power management command.
     * @param userIn the user running this command
     * @param nameIn the cobbler system name (prefix) to power on or off
     * @param operationIn the operation to run
     */
    public CobblerPowerCommand(User userIn, String nameIn, Operation operationIn) {
        super(userIn);
        operation = operationIn;
        String sep = ConfigDefaults.get().getCobblerNameSeparator();
        nameIn = nameIn.replace(' ', '_').replaceAll("[^a-zA-Z0-9_\\-\\.]", "");
        name = nameIn + sep + user.getOrg().getId();
    }

    private SystemRecord getSystemRecord() {
        CobblerConnection connection = getCobblerConnection();

        if (server != null) {
            String cobblerId = server.getCobblerId();
            if (!StringUtils.isEmpty(cobblerId)) {
                return SystemRecord.lookupById(connection, cobblerId);
            }
        }
        else if (name != null) {
            return SystemRecord.lookupByName(connection, name);
        }
        return null;
    }

    /**
     * Attempts to power on, off or reboot the server.
     * @return any errors
     */
    @Override
    public ValidatorError store() {
        SystemRecord systemRecord = getSystemRecord();
        if (systemRecord != null && systemRecord.getPowerType() != null) {
            boolean success = false;
            try {
                switch (operation) {
                case PowerOn:
                    success = systemRecord.powerOn();
                    break;
                case PowerOff:
                    success = systemRecord.powerOff();
                    break;
                default:
                    success = systemRecord.reboot();
                    break;
                }
            }
            catch (XmlRpcException e) {
                log.error(e);
            }
            if (success) {
                if (server != null) {
                    log.debug("Power management operation " + operation.toString() +
                            " on " + server.getId() + " succeded");
                    LocalizationService localizationService = LocalizationService
                            .getInstance();
                    ServerHistoryEvent event = new ServerHistoryEvent();
                    event.setCreated(new Date());
                    event.setServer(server);
                    event.setSummary(localizationService
                            .getPlainText("cobbler.powermanagement." +
                                    operation.toString().toLowerCase()));
                    String details = "System has been powered on via " +
                            localizationService.getPlainText("cobbler.powermanagement." +
                                    systemRecord.getPowerType());
                    event.setDetails(details);
                    server.getHistory().add(event);
                }
                else {
                    log.debug("Power management operation " + operation.toString() +
                            " on " + name + " succeded");
                }

                return null;
            }
            if (server != null) {
                log.error(operation.toString() + " on " + server.getId() + " failed");
            }
            else {
                log.error(operation.toString() + " on " + name + " failed");
            }
            return new ValidatorError("cobbler.powermanagement.command_failed");
        }
        return new ValidatorError("cobbler.powermanagement.not_configured");
    }
}
