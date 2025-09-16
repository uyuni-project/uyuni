/*
 * Copyright (c) 2022 SUSE LLC
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
package com.suse.manager.api;

/**
 * Thrown when a read-only user tries to call a read-write endpoint
 */
public class UserNotPermittedException extends Exception {

    /**
     * Constructs a {@link UserNotPermittedException} with no message
     */
    public UserNotPermittedException() {
        super();
    }

    /**
     * Constructs a {@link UserNotPermittedException} with the specified detail message
     * @param message the detail message
     */
    public UserNotPermittedException(String message) {
        super(message);
    }
}
