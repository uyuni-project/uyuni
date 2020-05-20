/**
 * Copyright (c) 2016 SUSE LLC
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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.security.PermissionException;

import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.IOFaultException;
import com.redhat.rhn.frontend.xmlrpc.ValidationException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.domain.dto.FormulaData;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.manager.formula.FormulaManager;
import com.redhat.rhn.manager.formula.FormulaUtil;
import com.redhat.rhn.manager.formula.InvalidFormulaException;

/**
 * FormulaHandler
 * @xmlrpc.namespace formula
 * @xmlrpc.doc Provides methods to access and modify formulas.
 */
public class FormulaHandler extends BaseHandler {

    private FormulaManager formulaManager;

    /**
     * Instantiates a new formula handler.
     *
     * @param formulaManagerIn the formula manager
     */
    public FormulaHandler(FormulaManager formulaManagerIn) {
        super();
        this.formulaManager = formulaManagerIn;
    }

    /**
     * List all installed formulas.
     * @param loggedInUser The current user
     * @return the list of formulas currently installed.
     *
     * @xmlrpc.doc Return the list of formulas currently installed.
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.returntype #array_single("string", "(formulas)")
     */
    public List<String> listFormulas(User loggedInUser) {
        return FormulaFactory.listFormulaNames();
    }

    /**
     * List the formulas of a server group.
     * @param loggedInUser The current user
     * @param systemGroupId The Id of the server group
     * @return the list of formulas the server group has.
     *
     * @xmlrpc.doc Return the list of formulas a server group has.
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("int", "systemGroupId")
     * @xmlrpc.returntype #array_single("string", "(formulas)")
     */
    public List<String> getFormulasByGroupId(User loggedInUser, Integer systemGroupId) {
        ManagedServerGroup group = ServerGroupFactory.lookupByIdAndOrg(new Long(systemGroupId), loggedInUser.getOrg());
        FormulaUtil.ensureUserHasPermissionsOnServerGroup(loggedInUser, group);
        return FormulaFactory.getFormulasByGroupId(systemGroupId.longValue());
   }

    /**
     * List the formulas applied directly to a server.
     * @param loggedInUser The current user
     * @param systemId The Id of the server
     * @return the list of formulas the server has.
     *
     * @xmlrpc.doc Return the list of formulas directly applied to a server.
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("int", "systemId")
     * @xmlrpc.returntype #array_single("string", "(formulas)")
     */
    public List<String> getFormulasByServerId(User loggedInUser, Integer systemId) {
        Server server = ServerFactory.lookupById(new Long(systemId));
        FormulaUtil.ensureUserHasPermissionsOnServer(loggedInUser, server);
        return FormulaFactory.getFormulasByMinionId(MinionServerFactory.getMinionId(systemId.longValue()));
    }

    /**
     * List the formulas applied to a server and all of his groups.
     * @param loggedInUser The current user
     * @param systemId The Id of the server
     * @return the list of formulas the server and his groups have.
     *
     * @xmlrpc.doc Return the list of formulas a server and all his groups have.
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("int", "systemId")
     * @xmlrpc.returntype #array_single("string", "(formulas)")
     */
    public List<String> getCombinedFormulasByServerId(User loggedInUser, Integer systemId) {
        Server server = ServerFactory.lookupById(new Long(systemId));
        FormulaUtil.ensureUserHasPermissionsOnServer(loggedInUser, server);
        return FormulaFactory.getCombinedFormulasByServerId(systemId.longValue());
    }

    /**
     * Set the formulas for a server group
     * @param loggedInUser The current user
     * @param systemGroupId The Id of the server group
     * @param formulas The formulas to apply to the server group.
     * @return 1 on success, exception thrown otherwise
     * @throws IOFaultException if an IOException occurs during saving
     *
     * @xmlrpc.doc Set the formulas of a server group.
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("int", "systemGroupId")
     * @xmlrpc.param #array_single("string", "formulaName")
     * @xmlrpc.returntype #return_int_success()
     */
    public int setFormulasOfGroup(User loggedInUser, Integer systemGroupId,
            List<String> formulas) throws IOFaultException {
        try {
            FormulaFactory.saveGroupFormulas(systemGroupId.longValue(), formulas,
                    loggedInUser.getOrg());
        }
        catch (ValidatorException e) {
            throw new ValidationException(e.getMessage(), e);
        }
        catch (IOException e) {
            throw new IOFaultException(e);
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
     * @xmlrpc.doc Set the formulas of a server.
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("int", "systemId")
     * @xmlrpc.param #array_single("string", "formulaName")
     * @xmlrpc.returntype #return_int_success()
     */
    public int setFormulasOfServer(User loggedInUser, Integer systemId,
            List<String> formulas) throws IOFaultException, InvalidParameterException {
        try {
            Server server = ServerFactory.lookupById(new Long(systemId));
            FormulaUtil.ensureUserHasPermissionsOnServer(loggedInUser, server);
            FormulaFactory.saveServerFormulas(MinionServerFactory.getMinionId(systemId.longValue()), formulas);
        }
        catch (PermissionException e) {
            throw new PermissionException(LocalizationService.getInstance().getMessage("formula.accessdenied"));
        }
        catch (ValidatorException e) {
            throw new ValidationException(e.getMessage(), e);
        }
        catch (IOException e) {
            throw new IOFaultException(e);
        }
        catch (UnsupportedOperationException e) {
            throw new InvalidParameterException(
                    "Provided systemId does not correspond to a minion");
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
     * @xmlrpc.doc Get the saved data for the specific formula against specific server
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("int", "systemId")
     * @xmlrpc.param #param("string", "formulaName")
     * @xmlrpc.returntype struct with saved formula data
     */
    public Map<String, Object> getSystemFormulaData(User loggedInUser, Integer systemId, String formulaName) {
        FormulaManager manager = FormulaManager.getInstance();
        Map<String, Object> savedData = manager.getSystemFormulaData(loggedInUser, formulaName, systemId.longValue());
        return savedData;
    }

    /**
     * Get the save data for the passed formula applied to the systems whose IDs match with the passed systems IDs
     * and all of the groups those systems are member of
     *
     * @param loggedInUser The current user
     * @param systemIds The system IDs
     * @param formulaName formula name
     * @return a list containing the saved data for the passed formula and the passed system IDs.
     *
     * @xmlrpc.doc Return the list of formulas a server and all his groups have.
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "formulaName")
     * @xmlrpc.param #array_single("int", "systemID")
     * @xmlrpc.returntype
     *   #array_begin()
     *     $FormulaDataSerializer
     *   #array_end()
     */
    public List<FormulaData> getCombinedFormulaDataByServerIds(User loggedInUser, String formulaName,
            List<Long> systemIds) {
        return this.formulaManager.getCombinedFormulaDataForSystems(loggedInUser, systemIds, formulaName);
    }

    /**
     * Get the saved data for the specific formula against specific group
     *
     * @param loggedInUser user
     * @param formulaName formula name
     * @param groupId group id
     * @return saved data as Json string
     * @xmlrpc.doc Get the saved data for the specific formula against specific group
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("int", "groupId")
     * @xmlrpc.param #param("string", "formulaName")
     * @xmlrpc.returntype struct with saved formula data
     */
    public Map<String, Object> getGroupFormulaData(User loggedInUser, Integer groupId, String formulaName) {
        FormulaManager manager = FormulaManager.getInstance();
        Map<String, Object> savedData = manager.getGroupFormulaData(loggedInUser, formulaName, groupId.longValue());
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
     * @xmlrpc.doc Set the formula form for the specified server.
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("int", "systemId")
     * @xmlrpc.param #param("string", "formulaName")
     * @xmlrpc.param struct content with the values for each field in the form
     * @xmlrpc.returntype #return_int_success()
     */
    public int setSystemFormulaData(User loggedInUser, Integer systemId, String formulaName, Map<String,
                Object> content) throws IOFaultException, InvalidParameterException {
        try {
            FormulaManager manager = FormulaManager.getInstance();
            boolean assigned = manager.hasSystemFormulaAssignedCombined(formulaName, systemId);
            if (assigned) {
                manager.validateInput(formulaName, content);
                manager.saveServerFormulaData(loggedInUser, systemId.longValue(), formulaName, content);
            }
            else {
                throw new InvalidParameterException("One of the system doesn't have formula assigned, please assign" +
                        " it first and try again");
            }
        }
        catch (IOException e) {
            throw new IOFaultException(e);
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
     * @xmlrpc.doc Set the formula form for the specified group.
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("int","groupId")
     * @xmlrpc.param #param("string", "formulaName")
     * @xmlrpc.param struct containing the values for each field in the form
     * @xmlrpc.returntype #return_int_success()
     */
    public int setGroupFormulaData(User loggedInUser, Integer groupId, String formulaName, Map<String,
            Object> content) throws IOFaultException, InvalidParameterException {
        try {
            FormulaManager manager = FormulaManager.getInstance();
            boolean assigned = manager.hasGroupFormulaAssigned(formulaName, groupId.longValue());
            if (assigned) {
                manager.validateInput(formulaName, content);
                manager.saveGroupFormulaData(loggedInUser, groupId.longValue(), formulaName, content);
            }
            else {
                throw new InvalidParameterException("Group doesn't have formula assigned, please assign it" +
                        "first and try again");
            }
        }
        catch (IOException e) {
            throw new IOFaultException(e);
        }
        catch (InvalidFormulaException e) {
            throw new ValidationException(e.getMessage());
        }
        return 1;
    }

}
