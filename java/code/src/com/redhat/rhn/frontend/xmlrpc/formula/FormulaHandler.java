/*
 * Copyright (c) 2016--2021 SUSE LLC
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
package com.redhat.rhn.frontend.xmlrpc.formula;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.dto.FormulaData;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.IOFaultException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.ValidationException;
import com.redhat.rhn.manager.formula.FormulaManager;
import com.redhat.rhn.manager.formula.FormulaUtil;
import com.redhat.rhn.manager.formula.InvalidFormulaException;

import com.suse.manager.api.ReadOnly;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.utils.Opt;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FormulaHandler
 * @apidoc.namespace formula
 * @apidoc.doc Provides methods to access and modify formulas.
 */
public class FormulaHandler extends BaseHandler {

    private final FormulaManager formulaManager;
    private final SaltApi saltApi;

    /**
     * Instantiates a new formula handler.
     *
     * @param formulaManagerIn the formula manager
     * @param saltApiIn the Salt API
     */
    public FormulaHandler(FormulaManager formulaManagerIn, SaltApi saltApiIn) {
        super();
        this.formulaManager = formulaManagerIn;
        this.saltApi = saltApiIn;
    }

    /**
     * List all installed formulas.
     * @param loggedInUser The current user
     * @return the list of formulas currently installed.
     *
     * @apidoc.doc Return the list of formulas currently installed.
     *
     * @apidoc.param #session_key()
     * @apidoc.returntype #array_single("string", "the list of formulas")
     */
    @ReadOnly
    public List<String> listFormulas(User loggedInUser) {
        return FormulaFactory.listFormulaNames();
    }

    /**
     * List the formulas of a server group.
     * @param loggedInUser The current user
     * @param systemGroupId The Id of the server group
     * @return the list of formulas the server group has.
     *
     * @apidoc.doc Return the list of formulas a server group has.
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "systemGroupId")
     * @apidoc.returntype #array_single("string", "the list of formulas")
     */
    @ReadOnly
    public List<String> getFormulasByGroupId(User loggedInUser, Integer systemGroupId) {
        ManagedServerGroup group = ServerGroupFactory
                .lookupByIdAndOrg(systemGroupId.longValue(), loggedInUser.getOrg());
        FormulaUtil.ensureUserHasPermissionsOnServerGroup(loggedInUser, group);
        return FormulaFactory.getFormulasByGroup(group);
   }

    /**
     * List the formulas applied directly to a server.
     * @param loggedInUser The current user
     * @param sid The Id of the server
     * @return the list of formulas the server has.
     *
     * @apidoc.doc Return the list of formulas directly applied to a server.
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "the system ID")
     * @apidoc.returntype #array_single("string", "the list of formulas")
     */
    @ReadOnly
    public List<String> getFormulasByServerId(User loggedInUser, Integer sid) {
        Server server = ServerFactory.lookupById(sid.longValue());
        FormulaUtil.ensureUserHasPermissionsOnServer(loggedInUser, server);
        return FormulaFactory.getFormulasByMinion(server.asMinionServer()
                .orElseThrow(() -> new UnsupportedOperationException("Not a Salt minion: " + sid)));
    }

    /**
     * List the formulas applied to a server and all of his groups.
     * @param loggedInUser The current user
     * @param sid The Id of the server
     * @return the list of formulas the server and his groups have.
     *
     * @apidoc.doc Return the list of formulas a server and all his groups have.
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "the system ID")
     * @apidoc.returntype #array_single("string", "the list of formulas")
     */
    @ReadOnly
    public List<String> getCombinedFormulasByServerId(User loggedInUser, Integer sid) {
        MinionServer minion = MinionServerFactory.lookupById(sid.longValue())
                .orElseThrow(() -> new InvalidParameterException(
                        "Provided systemId does not correspond to a minion"));
        FormulaUtil.ensureUserHasPermissionsOnServer(loggedInUser, minion);
        return FormulaFactory.getCombinedFormulasByServer(minion);
    }

    /**
     * Set the formulas for a server group
     * @param loggedInUser The current user
     * @param systemGroupId The Id of the server group
     * @param formulas The formulas to apply to the server group.
     * @return 1 on success, exception thrown otherwise
     * @throws IOFaultException if an IOException occurs during saving
     *
     * @apidoc.doc Set the formulas of a server group.
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "systemGroupId")
     * @apidoc.param #array_single("string", "formulas")
     * @apidoc.returntype #return_int_success()
     */
    public int setFormulasOfGroup(User loggedInUser, Integer systemGroupId,
            List<String> formulas) throws IOFaultException {
        try {
            ServerGroup group = ServerGroupFactory.lookupById(systemGroupId.longValue());
            FormulaFactory.saveGroupFormulas(group, formulas);
            List<String> minions = group.getServers().stream()
                    .map(Server::asMinionServer)
                    .flatMap(Opt::stream)
                    .map(MinionServer::getMinionId)
                    .collect(Collectors.toList());
            saltApi.refreshPillar(new MinionList(minions));
        }
        catch (ValidatorException e) {
            throw new ValidationException(e.getMessage(), e);
        }
        return 1;
    }

    /**
     * Set the formulas for a server
     * @param loggedInUser The current user
     * @param systemId The Id of the server
     * @param formulas The formulas to apply to the server group.
     * @return 1 on success, exception thrown otherwise
     * @throws IOFaultException if an IOException occurs during saving
     * @throws InvalidParameterException if the server is not a salt minion
     *
     * @apidoc.doc Set the formulas of a server.
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "the system ID")
     * @apidoc.param #array_single("string", "formulas")
     * @apidoc.returntype #return_int_success()
     */
    public int setFormulasOfServer(User loggedInUser, Integer systemId,
            List<String> formulas) throws IOFaultException, InvalidParameterException {
        try {
            MinionServer minion = MinionServerFactory.lookupById(systemId.longValue())
                    .orElseThrow(() -> new InvalidParameterException(
                            "Provided systemId does not correspond to a minion"));
            FormulaUtil.ensureUserHasPermissionsOnServer(loggedInUser, minion);
            FormulaFactory.saveServerFormulas(minion, formulas);
            saltApi.refreshPillar(new MinionList(minion.getMinionId()));
        }
        catch (PermissionException e) {
            throw new PermissionException(LocalizationService.getInstance().getMessage("formula.accessdenied"));
        }
        catch (ValidatorException e) {
            throw new ValidationException(e.getMessage(), e);
        }
        return 1;
    }

    /**
     * Get the saved data for the specific formula against specific server
     *
     * @param loggedInUser user
     * @param formulaName formula name
     * @param systemId system id
     * @return saved data as Json
     *
     * @apidoc.doc Get the saved data for the specific formula against specific server
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "the system ID")
     * @apidoc.param #param("string", "formulaName")
     * @apidoc.returntype #param("struct", "the saved formula data")
     */
    @ReadOnly
    public Map<String, Object> getSystemFormulaData(User loggedInUser, Integer systemId, String formulaName) {
        return formulaManager
                .getSystemFormulaData(loggedInUser, formulaName, systemId.longValue());
    }

    /**
     * Get the save data for the passed formula applied to the systems whose IDs match with the passed systems IDs
     * and all of the groups those systems are member of
     *
     * @param loggedInUser The current user
     * @param sids The system IDs
     * @param formulaName formula name
     * @return a list containing the saved data for the passed formula and the passed system IDs.
     *
     * @apidoc.doc Return the list of formulas a server and all his groups have.
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "formulaName")
     * @apidoc.param #array_single("int", "sids", "the list of system IDs")
     * @apidoc.returntype
     *   #return_array_begin()
     *     $FormulaDataSerializer
     *   #array_end()
     */
    @ReadOnly
    public List<FormulaData> getCombinedFormulaDataByServerIds(User loggedInUser, String formulaName,
            List<Integer> sids) {
        List<Long> ids = sids.stream()
                .map(Integer::longValue)
                .collect(Collectors.toList());
        return this.formulaManager.getCombinedFormulaDataForSystems(loggedInUser, ids, formulaName);
    }

    /**
     * Get the saved data for the specific formula against specific group
     *
     * @param loggedInUser user
     * @param formulaName formula name
     * @param groupId group id
     * @return saved data as Json string
     * @apidoc.doc Get the saved data for the specific formula against specific group
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "groupId")
     * @apidoc.param #param("string", "formulaName")
     * @apidoc.returntype #param("struct", "the saved formula data")
     */
    @ReadOnly
    public Map<String, Object> getGroupFormulaData(User loggedInUser, Integer groupId, String formulaName) {
        Map<String, Object> savedData = formulaManager
                .getGroupFormulaData(loggedInUser, formulaName, groupId.longValue());
        return savedData;
    }

    /**
     * Set the formula form data for the specified servers
     * @param loggedInUser The current user
     * @param systemId Id of the server
     * @param formulaName name of the formula that should be populated.
     * @param content Map containing the values for each field in the form.
     * @return 1 on success, exception thrown otherwise
     * @throws IOFaultException if an IOException occurs during saving
     * @throws InvalidParameterException if the server is not a salt minion
     *
     * @apidoc.doc Set the formula form for the specified server.
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "systemId")
     * @apidoc.param #param("string", "formulaName")
     * @apidoc.param #param_desc("struct", "content", "struct content with the values for each field in the form")
     * @apidoc.returntype #return_int_success()
     */
    public int setSystemFormulaData(User loggedInUser, Integer systemId, String formulaName, Map<String,
                Object> content) throws IOFaultException, InvalidParameterException {
        try {
            MinionServer server = MinionServerFactory.lookupById(systemId.longValue())
                    .orElseThrow(() -> new InvalidParameterException("Salt minion system not found: " + systemId));
            boolean assigned = formulaManager.hasSystemFormulaAssignedCombined(formulaName, server);
            if (assigned) {
                formulaManager.validateInput(formulaName, content);
                formulaManager.saveServerFormulaData(loggedInUser, systemId.longValue(), formulaName, content);
            }
            else {
                throw new InvalidParameterException("One of the system doesn't have formula assigned, please assign" +
                        " it first and try again");
            }
        }
        catch (InvalidFormulaException e) {
            throw new ValidationException(e.getMessage());
        }
        return 1;
    }

    /**
     * Set the formula form  data for the group
     * @param loggedInUser The current user
     * @param groupId  Id of the group
     * @param formulaName name of the formula that should be populated.
     * @param content Map containing the values for each field in the form.
     * @return 1 on success, exception thrown otherwise
     * @throws IOFaultException if an IOException occurs during saving
     * @throws InvalidParameterException if the server is not a salt minion
     *
     * @apidoc.doc Set the formula form for the specified group.
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param("int","groupId")
     * @apidoc.param #param("string", "formulaName")
     * @apidoc.param #param_desc("struct", "content", "struct containing the values for each field in the form")
     * @apidoc.returntype #return_int_success()
     */
    public int setGroupFormulaData(User loggedInUser, Integer groupId, String formulaName, Map<String,
            Object> content) throws IOFaultException, InvalidParameterException {
        try {
            ServerGroup group = ServerGroupFactory.lookupByIdAndOrg(groupId.longValue(), loggedInUser.getOrg());
            boolean assigned = formulaManager.hasGroupFormulaAssigned(formulaName, group);
            if (assigned) {
                formulaManager.validateInput(formulaName, content);
                formulaManager.saveGroupFormulaData(loggedInUser, groupId.longValue(), formulaName, content);
            }
            else {
                throw new InvalidParameterException("Group doesn't have formula assigned, please assign it" +
                        "first and try again");
            }
        }
        catch (InvalidFormulaException e) {
            throw new ValidationException(e.getMessage());
        }
        return 1;
    }

}
