/**
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
package com.redhat.rhn.frontend.xmlrpc.formula;

import java.io.IOException;
import java.util.List;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.domain.formula.FormulaFactory;


/**
 * FormulaHandler
 * @version $Rev$
 * @xmlrpc.namespace formula
 * @xmlrpc.doc Provides methods to access and modify formulas.
 */
public class FormulaHandler extends BaseHandler {

    /**
     * List all installed formulas.
     * @param loggedInUser The current user
     * @return the list of formulas currently installed.
     *
     * @xmlrpc.doc Return the list of formulas currently installed.
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.returntype
     *      #array()
     *          string
     *      #array_end()
     */
    public List<String> listFormulas(User loggedInUser) {
        return FormulaFactory.listFormulas();
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
     * @xmlrpc.returntype
     *      #array()
     *          string
     *      #array_end()
     */
    public List<String> getFormulasByGroupId(User loggedInUser, Integer systemGroupId) {
        return FormulaFactory.getFormulasByGroupId(systemGroupId.longValue());
    }

    /**
     * List the formulas of a server.
     * @param loggedInUser The current user
     * @param systemId The Id of the server
     * @return the list of formulas the server group has.
     *
     * @xmlrpc.doc Return the list of formulas a server has.
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("int", "systemId")
     * @xmlrpc.returntype
     *      #array()
     *          string
     *      #array_end()
     */
    public List<String> getFormulasByServerId(User loggedInUser, Integer systemId) {
        return FormulaFactory.getFormulasByServerId(systemId.longValue());
    }

    /**
     * Set the formulas for a server group
     * @param loggedInUser The current user
     * @param systemGroupId The Id of the server group
     * @param formulas The formulas to apply to the server group.
     * @return 1 on sucess, expcetion thrown otherwise
     * @throws IOException if an IOException occurs during saving
     *
     * @xmlrpc.doc Set the formulas of a server group.
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("int", "systemGroupId")
     * @xmlrpc.param #array_single("string", "formulaName")
     * @xmlrpc.returntype #return_int_success()
     */
    public int setFormulasOfGroup(User loggedInUser, Integer systemGroupId,
            List<String> formulas) throws IOException {
        FormulaFactory.saveServerGroupFormulas(systemGroupId.longValue(), formulas,
                loggedInUser.getOrg());
        return 1;
    }
}
