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

import com.suse.manager.webui.utils.gson.JSONBootstrapHosts;

import org.apache.commons.lang.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Input validation helper methods.
 */
public enum InputValidator {

    /**
     * Singleton instance
     */
    INSTANCE;

    /**
     * Validate input as sent from the minion bootstrapping UI.
     *
     * @param input the data as entered in the form
     * @return list of validation error messages
     */
    public List<String> validateBootstrapInput(JSONBootstrapHosts input) {
        List<String> errors = new LinkedList<>();

        if (StringUtils.isEmpty(input.getHost())) {
            errors.add("Host is required.");
        }
        if (StringUtils.isEmpty(input.getUser())) {
            errors.add("User is required.");
        }

        Optional<Integer> port = Optional.empty();
        if (StringUtils.isNotEmpty(input.getPort())) {
            try {
                port = Optional.of(Integer.valueOf(input.getPort()));
            }
            catch (NumberFormatException nfe) {
                errors.add("Given port is not a valid number.");
            }
        }
        if (port.filter(p -> p < 1 || p > 65535).isPresent()) {
            errors.add("Given port is outside of the valid range (1-65535).");
        }

        return errors;
    }
}
