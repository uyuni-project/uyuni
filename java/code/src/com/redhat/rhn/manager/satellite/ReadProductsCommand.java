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

import com.suse.manager.model.products.Product;
import com.suse.manager.model.products.ProductList;

import org.apache.log4j.Logger;
import org.apache.tools.ant.filters.StringInputStream;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Simple command class for reading products using mgr-ncc-sync.
 * User must be SAT_ADMIN to use this Command.
 */
public class ReadProductsCommand {

    private static Logger logger = Logger.getLogger(ReadProductsCommand.class);

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
     * Current command line
     *
     * @return the command line and arguments that
     * will be executed based on the command configuration.
     * (Useful for tests for example)
     */
    public List<String> commandLine() {
        List<String> args = new LinkedList<String>();
        args.add("/usr/bin/sudo");
        args.add("/usr/sbin/mgr-ncc-sync");
        args.add("--list-products-xml");
        return args;
    }

    /**
     * Reads products from mgr-ncc-sync.
     * @return validation errors, if any
     */
    public ValidatorError[] execute() {
        Executor e = new SystemCommandExecutor();
        ValidatorError[] errors = new ValidatorError[1];

        int exitcode = e.execute((String[]) commandLine().toArray(new String[0]));
        if (exitcode != 0) {
            errors[0] = new ValidatorError("restart.config.error");
            return errors;
        }
        else {
            xmlOutput = e.getLastCommandOutput();
            logger.debug("Output --> " + xmlOutput);
        }
        return null;
    }

    /**
     * Gets the products.
     * @return the products
     */
    public List<Product> getProducts() {
        return parse(new StringInputStream(xmlOutput));
    }

    /**
     * Parse {@link InputStream} into a List of {@link Product} objects.
     * @param stream an input stream
     * @return list of products
     */
    public List<Product> parse(InputStream stream) {
        ProductList result = null;
        Serializer serializer = new Persister();
        try {
            result = serializer.read(ProductList.class, stream);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result.getProducts();
    }
}
