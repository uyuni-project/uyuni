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

package com.redhat.rhn.domain.recurringactions;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * ServerFactory - the class used to fetch and store
 * com.redhat.rhn.domain.server.RecurringAction objects from the database.
 * @version $Rev$
 */
public class RecurringActionFactory extends HibernateFactory {

    private static final Logger LOG = Logger.getLogger(RecurringActionFactory.class);
    private static final RecurringActionFactory INSTANCE = new RecurringActionFactory();

    /**
     * List minion recurring actions with minion id.
     *
     * @param id - id of the minion
     * @return list of minion recurring actions
     */
    public static List<MinionRecurringAction> listMinionRecurringActions(Long id) { // TODO: Do we need user here?
       return getSession().createQuery("SELECT action FROM MinionRecurringAction action " +
               "WHERE action.minion.id = :mid")
               .setParameter("mid", id)
               .list();
    }

    /**
     * List group recurring actions with group id.
     *
     * @param id - id of the group
     * @return list of group recurring actions
     */
    public static List<GroupRecurringAction> listGroupRecurringActions(Long id) { // TODO: Do we need user here?
        return getSession().createQuery("SELECT action FROM GroupRecurringAction action " +
                "WHERE action.group.id = :gid")
                .setParameter("gid", id)
                .list();
    }

    /**
     * List org recurring actions with org id.
     *
     * @param id - id of the organization
     * @return list of org recurring actions
     */
    public static List<OrgRecurringAction> listOrgRecurringActions(Long id) { // TODO: Do we need user here?
        return getSession().createQuery("SELECT action FROM OrgRecurringAction action " +
                "WHERE action.org.id = :oid")
                .setParameter("oid", id)
                .list();
    }

    /**
     * Lookup recurring action with given id.
     *
     * @param id - id of the recurring action
     * @return optional of matching recurring action
     */
    public static Optional<RecurringAction> lookupById(long id) {
        return getSession().createQuery("SELECT action FROM RecurringAction action " +
                "WHERE action.id = :id")
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    /**
     * Lookup recurring action with given taskomatic schedule name
     *
     * @param scheduleName the name of recurring action taskomatic schedule
     * @return optional of matching recurring action
     */
    public static Optional<RecurringAction> lookupByJobName(String scheduleName) {
        long id = Long.parseLong(scheduleName.replace(RecurringAction.RECURRING_ACTION_PREFIX, ""));
        return lookupById(id);
    }

    /**
     * Save a recurring action
     *
     * @param action the action to save
     */
    public static void save(RecurringAction action) {
        INSTANCE.saveObject(action);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
