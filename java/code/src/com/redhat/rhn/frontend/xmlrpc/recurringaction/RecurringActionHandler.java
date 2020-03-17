/**
 * Copyright (c) 2020 SUSE LLC
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

package com.redhat.rhn.frontend.xmlrpc.recurringaction;

import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.EntityExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.EntityNotExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.InvalidArgsException;
import com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.recurringactions.RecurringActionManager;

import org.hibernate.HibernateException;

import java.util.List;
import java.util.Map;

public class RecurringActionHandler extends BaseHandler {

    /* helper method */
    private RecurringAction.Type getEntityType(String entityType) {
        try {
            return RecurringAction.Type.valueOf(entityType.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            throw new InvalidArgsException("Type \"" + entityType + "\" does not exist");
        }
    }

    /**
     * Return a list of recurring actions for a given entity.
     *
     * @param loggedInUser The current user
     * @param entityId the id of the entity
     * @param entityType type of the entity
     * @return the list of recurring actions
     *
     * @xmlrpc.doc Return a list of recurring actions for a given entity.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "entityType", "Type of the target entity")
     * @xmlrpc.param #param_desc("int", "entityId", "Id of the target entity")
     * @xmlrpc.returntype
     *      #array()
     *          $RecurringActionSerializer
     *      #array_end()
     */
    public List<? extends RecurringAction> listByEntity(User loggedInUser, String entityType, Integer entityId) {
        List<? extends RecurringAction> schedules;
        try {
            switch (getEntityType(entityType)) {
                case MINION:
                    schedules = RecurringActionManager.listMinionRecurringActions(entityId, loggedInUser);
                    break;
                case GROUP:
                    schedules = RecurringActionManager.listGroupRecurringActions(entityId, loggedInUser);
                    break;
                case ORG:
                    schedules = RecurringActionManager.listOrgRecurringActions(entityId, loggedInUser);
                    break;
                default:
                    throw new IllegalStateException("Unsupported type " + entityType);
            }
            return schedules;
        }
        catch (PermissionException e) {
            throw new PermissionCheckFailureException(e.getMessage());
        }
    }
}
