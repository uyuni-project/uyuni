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
import com.redhat.rhn.manager.EntityExistsException;

/**
 * Fault that conveys an information about existence of an Entity
 */
public class EntityExistsFaultException extends FaultException {

    private static final int ERROR_CODE = 10100;
    private static final String ERROR_LABEL = "entityExists";

    /**
     * Standard constructor
     *
     * @param cause the EntityExistsException cause
     */
    public EntityExistsFaultException(EntityExistsException cause) {
        super(ERROR_CODE, ERROR_LABEL, cause.getMessage(), cause);
    }

    /**
     * Standard constructor
     *
     * @param entity the entity
     */
    public EntityExistsFaultException(Object entity) {
        super(ERROR_CODE, ERROR_LABEL, entity.toString());
    }
}
