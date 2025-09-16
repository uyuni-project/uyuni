/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.manager.access;

/**
 * Thrown when permissions for a default access group is attempted to be altered
 */
public class DefaultRoleException extends Exception {
    /**
     * Instantiate a new exception
     * @param message the exception message
     */
    public DefaultRoleException(String message) {
        super(message);
    }
}
