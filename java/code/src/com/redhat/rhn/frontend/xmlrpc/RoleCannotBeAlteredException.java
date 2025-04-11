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

package com.redhat.rhn.frontend.xmlrpc;

import com.redhat.rhn.FaultException;

/**
 * Fault that default RBAC roles cannot be altered
 */
public class RoleCannotBeAlteredException extends FaultException {

    private static final int ERROR_CODE = 2001;
    private static final String ERROR_LABEL = "defaultRolesCannotBeAltered";


    /**
     * Instantiate a new exception
     * @param cause the cause
     */
    public RoleCannotBeAlteredException(Exception cause) {
        super(ERROR_CODE, ERROR_LABEL, cause.getMessage(), cause);
    }

    /**
     * Instantiate a new exception with an entity
     * @param identifier the entity to describe the exception
     */
    public RoleCannotBeAlteredException(Object identifier) {
        super(ERROR_CODE, ERROR_LABEL, identifier.toString());
    }
}
