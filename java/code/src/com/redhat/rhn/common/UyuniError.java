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

import java.io.Serializable;

/**
 * Represents a generic error in scope of uyuni
 */
public class UyuniError implements Serializable {
    private final String message;

    /**
     * Constructor
     * @param messageIn the error message
     */
    public UyuniError(String messageIn) {
        this.message = messageIn;
    }

    public String getMessage() {
        return message;
    }
}
