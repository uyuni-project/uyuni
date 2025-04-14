/*
 * Copyright (c) 2025 SUSE LLC
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

package com.redhat.rhn.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a collection of errors that can be reported.
 */
public class UyuniErrorReport {
    private final List<UyuniError> errors = Collections.synchronizedList(new ArrayList<>());

    /**
     * Registers a new error in the error report.
     *
     * @param message   The error message.
     */
    public void register(String message) {
        errors.add(new UyuniError(message));
    }

    /**
     * Checks if any errors have been registered.
     *
     * @return true if there are errors; false otherwise.
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Returns a copy of the current list of errors.
     *
     * @return A copy of the errors list.
     */
    public List<UyuniError> getErrors() {
        return new ArrayList<>(errors);
    }

    /**
     * Logs the errors following a UyuniReportStrategy.
     * @param strategy The reporting strategy.
     */
    public void report(UyuniReportStrategy<UyuniError> strategy) {
        strategy.report(errors);
    }

    /**
     * Logs the errors using the default validation reporting strategy.
     */
    public void report() {
        ErrorReportingStrategies.validationReportingStrategy().report(errors);
    }

}
