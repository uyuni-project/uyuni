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

package com.redhat.rhn.manager;

/**
 * Conveys information about non-existence of an Entity
 */
public class EntityNotExistsException extends RuntimeException {

    /**
     * Standard constructor.
     *
     * @param entityIdentifier the identifier of the entity (id, label...)
     */
    public EntityNotExistsException(Object entityIdentifier) {
        super("Entity does not exist: " + entityIdentifier);
    }

    /**
     * Standard constructor.
     *
     * @param entityClass the entity class
     * @param entityIdentifier the identifier of the entity (id, label...)
     */
    public EntityNotExistsException(Class entityClass, Object entityIdentifier) {
        super("Entity of class: " + entityClass + " does not exist: " + entityIdentifier);
    }
}
