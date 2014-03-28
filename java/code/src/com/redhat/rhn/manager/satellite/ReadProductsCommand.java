/**
 * Copyright (c) 2014 SUSE
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
package com.redhat.rhn.manager.satellite;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;

import java.util.LinkedList;
import java.util.List;

/**
 * Simple command class for reading products using mgr-ncc-sync.
 * User must be SAT_ADMIN to use this Command.
 */
public class ReadProductsCommand {

    private User user;
    private String xmlOutput;

    /**
     * Construct the Command
     * @param userIn who wants to restart
     */
    public ReadProductsCommand(User userIn) {
        this.user = userIn;
        if (!this.user.hasRole(RoleFactory.SAT_ADMIN)) {
            throw new IllegalArgumentException("Must be SAT_ADMIN" +
                    "to read the products");
        }
    }

    /**
     * {@inheritDoc}
     */
    public ValidatorError[] readProducts() {
        Executor e = new SystemCommandExecutor();
        ValidatorError[] errors = new ValidatorError[1];
        List<String> args = new LinkedList<String>();
        args.add("/usr/bin/sudo");
        args.add("/usr/sbin/mgr-ncc-sync");
        args.add("--list-products-xml");

        int exitcode = e.execute((String[]) args.toArray(new String[0]));
        if (exitcode != 0) {
            errors[0] = new ValidatorError("restart.config.error");
            return errors;
        }
        else {
            xmlOutput = e.getLastCommandOutput();
        }
        return null;
    }

    /**
     * Return XML output of mgr-ncc-sync.
     * @return xml output as string
     */
    public String getXMLOutput() {
        return xmlOutput;
    }
}
