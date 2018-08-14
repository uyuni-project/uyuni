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
package com.suse.manager.webui.utils;

import com.redhat.rhn.common.validator.HostPortValidator;
import com.suse.manager.webui.utils.gson.BootstrapHostsJson;

import org.apache.commons.lang.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Input validation helper methods.
 */
public enum InputValidator {

    /**
     * Singleton instance
     */
    INSTANCE;

    // Allow letters (of all languages), numbers, '.', '/', '\' and '-'
    private static final Pattern USERNAME = Pattern.compile("^[\\p{L}\\p{N}.-/\\\\]*$");

    /**
     * Validate input as sent from the minion bootstrapping UI.
     *
     * @param input the data as entered in the form
     * @return list of validation error messages
     */
    public List<String> validateBootstrapInput(BootstrapHostsJson input) {
        List<String> errors = new LinkedList<>();

        errors.addAll(validateBootstrapSSHManagedInput(input));

        String user = input.getUser();
        if (StringUtils.isEmpty(user) || !USERNAME.matcher(user).matches()) {
            errors.add("Non-valid user. Allowed characters are: letters, numbers, '.'," +
                    " '\\' and '/'");
        }

        String port = input.getPort();
        if (StringUtils.isNotEmpty(port) &&
                !HostPortValidator.getInstance().isValidPort(port)) {
            errors.add("Port must be a number within range 1-65535.");
        }

        return errors;
    }

    /**
     * Validate input as sent from the minion bootstrapping UI for salt-ssh minions.
     *
     * @param input the data as entered in the form
     * @return list of validation error messages
     */
    public List<String> validateBootstrapSSHManagedInput(BootstrapHostsJson input) {
        List<String> errors = new LinkedList<>();

        String host = input.getHost();
        if (StringUtils.isEmpty(host) ||
                !HostPortValidator.getInstance().isValidHost(host)) {
            errors.add("Invalid host name.");
        }

        return errors;
    }
}
