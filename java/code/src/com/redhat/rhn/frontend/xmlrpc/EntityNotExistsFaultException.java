/**
 * Copyright (c) 2019 SUSE LLC
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

package com.redhat.rhn.frontend.xmlrpc;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.manager.EntityNotExistsException;

/**
 * Fault that conveys an information about nonexistence of an Entity
 */
public class EntityNotExistsFaultException extends FaultException {

    private static final int ERROR_CODE = 10101;
    private static final String ERROR_LABEL = "entityNotExists";

    /**
     * Standard constructor
     *
     * @param cause the EntityNotExistsException cause
     */
    public EntityNotExistsFaultException(EntityNotExistsException cause) {
        super(ERROR_CODE, ERROR_LABEL, cause.getMessage(), cause);
    }

    /**
     * Standard constructor
     *
     * @param identifier the entity identifier
     */
    public EntityNotExistsFaultException(Object identifier) {
        super(ERROR_CODE, ERROR_LABEL, identifier.toString());
    }
}
