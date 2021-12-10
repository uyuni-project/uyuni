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
package com.redhat.rhn.frontend.xmlrpc.system.provisioning.powermanagement;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.kickstart.PowerManagementAction;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchSystemException;
import com.redhat.rhn.frontend.xmlrpc.PowerManagementOperationFailedException;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerPowerCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerPowerSettingsUpdateCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerUnregisteredPowerSettingsUpdateCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.log4j.Logger;
import org.cobbler.SystemRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PowerManagementHandler
 * @xmlrpc.namespace system.provisioning.powermanagement
 * @xmlrpc.doc Provides methods to access and modify power management for systems.
 * Some functions exist in 2 variants. Either with server id or with a name.
 * The function with server id is useful when a system exists with a full profile.
 * Everybody allowed to manage that system can execute these functions.
 * The variant with name expects a cobbler system name prefix. These functions
 * enhance the name by adding the org id of the user to limit access to systems
 * from the own organization. Additionally Org Admin permissions are required to
 * call these functions.
 */
public class PowerManagementHandler extends BaseHandler {
    private static Logger log = Logger.getLogger(PowerManagementHandler.class);

    /**
     * Return a list of available power management types
     *
     * @param loggedInUser the user
     * @return a list of available power management types
     *
     * @xmlrpc.doc Return a list of available power management types
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype #array_single("string", "power management types")
     */
    public List<String> listTypes(User loggedInUser) {
        String typeString = ConfigDefaults.get().getCobblerPowerTypes();
        if (typeString != null) {
            return Arrays.asList(typeString.split(" *, *"));
        }
        return new ArrayList<>();
    }

    /**
     * Get current power management settings of the given system
     *
     * @param loggedInUser the user
     * @param serverId the requested server id
     * @return current power management settings when available
     *
     * @xmlrpc.doc Get current power management settings of the given system
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "serverId")
     * @xmlrpc.returntype
     *  #struct_begin("powerManagementParameters")
     *    #prop_desc("string", "powerType", "Power management type")
     *    #prop_desc("string", "powerAddress", "IP address for power management")
     *    #prop_desc("string", "powerUsername", "The Username")
     *    #prop_desc("string", "powerPassword", "The Password")
     *    #prop_desc("string", "powerId", "Identifier")
     *  #struct_end()
     */
    public Map<String, String> getDetails(User loggedInUser, Integer serverId) {
        SystemRecord record = SystemRecord.lookupById(
                CobblerXMLRPCHelper.getConnection(loggedInUser),
                lookupServer(loggedInUser, serverId).getCobblerId());
        return getDetails(loggedInUser, record);
    }

    /**
     * Get current power management settings of the given system
     *
     * @param loggedInUser the user
     * @param nameIn the cobbler name prefix
     * @return current power management settings when available
     *
     * @xmlrpc.doc Get current power management settings of the given system
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "name")
     * @xmlrpc.returntype
     *  #struct_begin("powerManagementParameters")
     *    #prop_desc("string", "powerType", "Power management type")
     *    #prop_desc("string", "powerAddress", "IP address for power management")
     *    #prop_desc("string", "powerUsername", "The Username")
     *    #prop_desc("string", "powerPassword", "The Password")
     *    #prop_desc("string", "powerId", "Identifier")
     *  #struct_end()
     */
    public Map<String, String> getDetails(User loggedInUser, String nameIn) {
        ensureOrgAdmin(loggedInUser);
        SystemRecord record = lookupExistingCobblerRecord(loggedInUser, nameIn);
        return getDetails(loggedInUser, record);
    }

    private Map<String, String> getDetails(User loggedInUser, SystemRecord record) {
        Map<String, String> result = new HashMap<>();
        List<String> types = listTypes(loggedInUser);
        if (record == null) {
            result.put(PowerManagementAction.POWER_TYPE, types.get(0));
        }
        else {
            result.put(PowerManagementAction.POWER_TYPE, record.getPowerType());
            result.put(PowerManagementAction.POWER_ADDRESS, record.getPowerAddress());
            result.put(PowerManagementAction.POWER_USERNAME, record.getPowerUsername());
            result.put(PowerManagementAction.POWER_PASSWORD, record.getPowerPassword());
            result.put(PowerManagementAction.POWER_ID, record.getPowerId());
        }
        return result;
    }

    /**
     * Set power management settings for the given system
     *
     * @param loggedInUser the user
     * @param serverId the requested server id
     * @param data power management parameters
     * @return current power management settings when available
     *
     * @xmlrpc.doc Get current power management settings of the given system
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "serverId")
     * @xmlrpc.param
     *  #struct_begin("data")
     *    #prop_desc("string", "powerType", "Power management type")
     *    #prop_desc("string", "powerAddress", "IP address for power management")
     *    #prop_desc("string", "powerUsername", "The Username")
     *    #prop_desc("string", "powerPassword", "The Password")
     *    #prop_desc("string", "powerId", "Identifier")
     *  #struct_end()
     * @xmlrpc.returntype #return_int_success()
     */
    public int setDetails(User loggedInUser, Integer serverId, Map<String, String> data) {
        CobblerPowerSettingsUpdateCommand cmd = new CobblerPowerSettingsUpdateCommand(
                loggedInUser, lookupServer(loggedInUser, serverId),
                data.get(PowerManagementAction.POWER_TYPE),
                data.get(PowerManagementAction.POWER_ADDRESS),
                data.get(PowerManagementAction.POWER_USERNAME),
                data.get(PowerManagementAction.POWER_PASSWORD),
                data.get(PowerManagementAction.POWER_ID));
        ValidatorError error = cmd.store();
        if (error != null) {
            throw new InvalidParameterException(error.getMessage());
        }

        return 1;
    }

    /**
     * Set power management settings for the given system
     *
     * @param loggedInUser the user
     * @param nameIn the cobbler name prefix
     * @param data power management parameters
     * @return current power management settings when available
     *
     * @xmlrpc.doc Get current power management settings of the given system
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "name")
     * @xmlrpc.param
     *  #struct_begin("data")
     *    #prop_desc("string", "powerType", "Power management type")
     *    #prop_desc("string", "powerAddress", "IP address for power management")
     *    #prop_desc("string", "powerUsername", "The Username")
     *    #prop_desc("string", "powerPassword", "The Password")
     *    #prop_desc("string", "powerId", "Identifier")
     *  #struct_end()
     * @xmlrpc.returntype #return_int_success()
     */
    public int setDetails(User loggedInUser, String nameIn, Map<String, String> data) {
        ensureOrgAdmin(loggedInUser);
        CobblerUnregisteredPowerSettingsUpdateCommand cmd =
                new CobblerUnregisteredPowerSettingsUpdateCommand(
                loggedInUser, nameIn,
                data.get(PowerManagementAction.POWER_TYPE),
                data.get(PowerManagementAction.POWER_ADDRESS),
                data.get(PowerManagementAction.POWER_USERNAME),
                data.get(PowerManagementAction.POWER_PASSWORD),
                data.get(PowerManagementAction.POWER_ID));
        ValidatorError error = cmd.store();
        if (error != null) {
            throw new InvalidParameterException(error.getMessage());
        }

        return 1;
    }

    /**
     * Execute power management action 'powerOn'
     *
     * @param loggedInUser the user
     * @param serverId the requested server id
     * @return 1 on success
     *
     * @xmlrpc.doc Execute power management action 'powerOn'
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "serverId")
     * @xmlrpc.returntype #return_int_success()
     */
    public int powerOn(User loggedInUser, Integer serverId) {
        ValidatorError error = new CobblerPowerCommand(loggedInUser, lookupServer(loggedInUser, serverId),
                CobblerPowerCommand.Operation.PowerOn).store();
        if (error != null) {
            log.error("Power management action 'powerOn' failed");
            throw new PowerManagementOperationFailedException(error.getMessage());
        }
        log.info("Power management action 'powerOn' succeeded");
        return 1;
    }

    /**
     * Execute power management action 'powerOn'
     *
     * @param loggedInUser the user
     * @param nameIn the cobbler name prefix
     * @return 1 on success
     *
     * @xmlrpc.doc Execute power management action 'powerOn'
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "name")
     * @xmlrpc.returntype #return_int_success()
     */
    public int powerOn(User loggedInUser, String nameIn) {
        ensureOrgAdmin(loggedInUser);
        ValidatorError error = new CobblerPowerCommand(loggedInUser, nameIn,
                CobblerPowerCommand.Operation.PowerOn).store();
        if (error != null) {
            log.error("Power management action 'powerOn' failed");
            throw new PowerManagementOperationFailedException(error.getMessage());
        }
        log.info("Power management action 'powerOn' succeeded");
        return 1;
    }

    /**
     * Execute power management action 'powerOff'
     *
     * @param loggedInUser the user
     * @param serverId the requested server id
     * @return 1 on success
     *
     * @xmlrpc.doc Execute power management action 'powerOff'
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "serverId")
     * @xmlrpc.returntype #return_int_success()
     */
    public int powerOff(User loggedInUser, Integer serverId) {
        ValidatorError error = new CobblerPowerCommand(loggedInUser, lookupServer(loggedInUser, serverId),
                CobblerPowerCommand.Operation.PowerOff).store();
        if (error != null) {
            log.error("Power management action 'powerOff' failed");
            throw new PowerManagementOperationFailedException(error.getMessage());
        }
        log.info("Power management action 'powerOff' succeeded");
        return 1;
    }

    /**
     * Execute power management action 'powerOff'
     *
     * @param loggedInUser the user
     * @param nameIn the cobbler name prefix
     * @return 1 on success
     *
     * @xmlrpc.doc Execute power management action 'powerOff'
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "name")
     * @xmlrpc.returntype #return_int_success()
     */
    public int powerOff(User loggedInUser, String nameIn) {
        ensureOrgAdmin(loggedInUser);
        ValidatorError error = new CobblerPowerCommand(loggedInUser, nameIn,
                CobblerPowerCommand.Operation.PowerOff).store();
        if (error != null) {
            log.error("Power management action 'powerOff' failed");
            throw new PowerManagementOperationFailedException(error.getMessage());
        }
        log.info("Power management action 'powerOff' succeeded");
        return 1;
    }

    /**
     * Execute power management action 'Reboot'
     *
     * @param loggedInUser the user
     * @param serverId the requested server id
     * @return 1 on success
     *
     * @xmlrpc.doc Execute power management action 'Reboot'
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "serverId")
     * @xmlrpc.returntype #return_int_success()
     */
    public int reboot(User loggedInUser, Integer serverId) {
        ValidatorError error = new CobblerPowerCommand(loggedInUser, lookupServer(loggedInUser, serverId),
                CobblerPowerCommand.Operation.Reboot).store();
        if (error != null) {
            log.error("Power management action 'reboot' failed");
            throw new PowerManagementOperationFailedException(error.getMessage());
        }
        log.info("Power management action 'reboot' succeeded");
        return 1;
    }

    /**
     * Execute power management action 'Reboot'
     *
     * @param loggedInUser the user
     * @param nameIn the cobbler name prefix
     * @return 1 on success
     *
     * @xmlrpc.doc Execute power management action 'Reboot'
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "name")
     * @xmlrpc.returntype #return_int_success()
     */
    public int reboot(User loggedInUser, String nameIn) {
        ensureOrgAdmin(loggedInUser);
        ValidatorError error = new CobblerPowerCommand(loggedInUser, nameIn,
                CobblerPowerCommand.Operation.Reboot).store();
        if (error != null) {
            log.error("Power management action 'reboot' failed");
            throw new PowerManagementOperationFailedException(error.getMessage());
        }
        log.info("Power management action 'reboot' succeeded");
        return 1;
    }

    /**
     * Return power status of a system
     *
     * @param loggedInUser the user
     * @param serverId the requested server id
     * @return 1 on success
     *
     * @xmlrpc.doc Execute powermanagement actions
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "serverId")
     * @xmlrpc.param #param("string", "action")
     * @xmlrpc.returntype #param_desc("boolean", "status", "True when power is on, otherwise False")
     */
    public boolean getStatus(User loggedInUser, Integer serverId) {
        SystemRecord record = SystemRecord.lookupById(
                CobblerXMLRPCHelper.getConnection(loggedInUser),
                lookupServer(loggedInUser, serverId).getCobblerId());
        return record.getPowerStatus();
    }

    /**
     * Return power status of a system
     *
     * @param loggedInUser the user
     * @param nameIn the requested server name (prefix)
     * @return 1 on success
     *
     * @xmlrpc.doc Execute powermanagement actions
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "name")
     * @xmlrpc.param #param("string", "action")
     * @xmlrpc.returntype #param_desc("boolean", "status", "True when power is on, otherwise False")
     */
    public boolean getStatus(User loggedInUser, String nameIn) {
        ensureOrgAdmin(loggedInUser);
        SystemRecord record = lookupExistingCobblerRecord(loggedInUser, nameIn);
        if (record == null) {
            throw new NoSuchSystemException();
        }
        return record.getPowerStatus();
    }

    private SystemRecord lookupExistingCobblerRecord(User loggedInUser, String label) {
        String sep = ConfigDefaults.get().getCobblerNameSeparator();
        label = label.replace(' ', '_').replaceAll("[^a-zA-Z0-9_\\-\\.]", "");
        String name = label + sep + loggedInUser.getOrg().getId();
        SystemRecord rec = SystemRecord.lookupByName(
                CobblerXMLRPCHelper.getConnection(loggedInUser), name);
        if (rec == null) {
            log.error("System with cobbler name " + name + " not found.");
        }
        return rec;
    }

    private Server lookupServer(User loggedInUser, Integer serverId) {
        Server server = null;
        try {
            server = SystemManager.lookupByIdAndUser(serverId.longValue(), loggedInUser);
        }
        catch (LookupException e) {
            throw new NoSuchSystemException();
        }
        return server;
    }
}
