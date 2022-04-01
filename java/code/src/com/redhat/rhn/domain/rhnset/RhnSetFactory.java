/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.domain.rhnset;

import com.redhat.rhn.common.db.ConstraintViolationException;
import com.redhat.rhn.common.db.WrappedSQLException;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * RhnSetFactory
 */
public class RhnSetFactory extends HibernateFactory {
    private static final String CATALOG = "Set_queries";

    private static RhnSetFactory singleton = new RhnSetFactory();
    private static Logger log = LogManager.getLogger(RhnSetFactory.class);

    /**
     * Constructs the RhnSetFactory, marked private
     * since all methods are static.
     */
    private RhnSetFactory() {
    }

    /**
     * {@inheritDoc}
     */
    protected Logger getLogger() {
        return log;
    }

    /**
     * Finds the RhnSet which matches the given uid and label.
     * Returns null if no matches found.
     * @param uid Userid of RhnSet
     * @param label Label of RhnSet
     * @param cleanup TODO
     * @return the RhnSet which matched the given uid and label.
     */
    public static RhnSet lookupByLabel(Long uid, String label, SetCleanup cleanup) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", uid);
        params.put("label", label);
        SelectMode m = ModeFactory.getMode(CATALOG, "lookup_set");
        DataResult elements = m.execute(params);
        RhnSetImpl result = singleton.createFromList(elements, cleanup);
        if (result != null) {
            result.sync();
        }
        return result;
    }

    /**
     * Creates an RhnSet from a List of RhnSetElements.
     * Returns null if elements are null or contain no items.
     * @param elements list of RhnSetElements
     * @param cleanup the cleanup that should be run when the set is stored
     * @return a newly created RhnSet with the given userid and label, and
     * all elements populated.
     */
    private RhnSetImpl createFromList(List elements, SetCleanup cleanup) {
        if (elements == null || elements.isEmpty()) {
            return null;
        }

        RhnSetElement element = (RhnSetElement)elements.get(0);
        RhnSetImpl set =
            new RhnSetImpl(element.getUserId(), element.getLabel(), cleanup);
        for (Object elementIn : elements) {
            element = (RhnSetElement) elementIn;
            set.addElement(element);
        }

        return set;
    }

    /**
     * Returns a new RhnSet.
     * @param userid userid associated with this set.
     * @param label set label.
     * @param cleanup the cleanup that should be run when the set is stored
     * @return a newly created RhnSet with the given userid and label,
     * with an empty elements list.
     */
    public static RhnSet createRhnSet(Long userid, String label, SetCleanup cleanup) {
        return new RhnSetImpl(userid, label, cleanup);
    }

    /**
     * Persists the given RhnSet to the database.
     * @param set RhnSet to be persisted.
     */
    public static void save(RhnSet set) {
        RhnSetImpl simpl = (RhnSetImpl) set;
        // The updates really need to be batched
        if (simpl.isSynced() && !simpl.getElements().isEmpty()) {
            WriteMode deleteEl3 = writeMode("delete_from_set_el3");
            WriteMode deleteEl2 = writeMode("delete_from_set_el2");
            WriteMode deleteEl1 = writeMode("delete_from_set_el1");
            for (RhnSetElement current : simpl.getRemoved()) {
                executeMode(current, deleteEl3, deleteEl2, deleteEl1);
            }
        }
        else {
            removeByLabel(simpl.getUserId(), simpl.getLabel());
        }

        Set added;
        if (!simpl.isSynced()) {
            added = simpl.getElements();
        }
        else {
            added = simpl.getAdded();
        }
        WriteMode insertEl3 = ModeFactory.getWriteMode(CATALOG, "add_to_set_el3");
        WriteMode insertEl2 = ModeFactory.getWriteMode(CATALOG, "add_to_set_el2");
        WriteMode insertEl1 = ModeFactory.getWriteMode(CATALOG, "add_to_set_el1");

        for (Object oIn : added) {
            RhnSetElement current = (RhnSetElement) oIn;
            try {
                executeMode(current, insertEl3, insertEl2, insertEl1);
            }
            catch (ConstraintViolationException e) {
                // a concurrent transaction has already inserted this row
                // and COMMITted. This is tolerable and can happen because
                // the default transaction isolation level is READ
                // COMMITTED, thus this exception can be safely ignored
            }
            catch (WrappedSQLException e) {
                // see ConstraintViolationException
            }
        }
        if (!added.isEmpty()) {
            simpl.getCleanup().cleanup(simpl);
        }
        simpl.sync();
    }

    /**
     * Cleanup the set. That is useful, when some of the items included in the set were
     * removed from database. That might have invalidated part of the set.
     * @param set RhnSet to cleanup.
     */
    public static void cleanup(RhnSet set) {
        RhnSetImpl simpl = (RhnSetImpl) set;
        simpl.getCleanup().cleanup(simpl); // Even palindrom can save the day.
    }

    private static WriteMode writeMode(String modeName) {
        return ModeFactory.getWriteMode(CATALOG, modeName);
    }

    private static void executeMode(RhnSetElement elem,
            WriteMode el3, WriteMode el2, WriteMode el1) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", elem.getUserId());
        params.put("label", elem.getLabel());
        params.put("el_one", elem.getElement());

        if (elem.getElementThree() == null && elem.getElementTwo() == null) {
            el1.executeUpdate(params);
        }
        else if (elem.getElementThree() == null) {
            params.put("el_two", elem.getElementTwo());
            el2.executeUpdate(params);
        }
        else {
            params.put("el_three", elem.getElementThree());
            params.put("el_two", elem.getElementTwo());
            el3.executeUpdate(params);
        }
    }

    /**
     * Removes a set by label and userid.
     * @param userId The userid associated with the set.
     * @param label The set's label.
     */
    public static void removeByLabel(Long userId, String label) {
        WriteMode m = ModeFactory.getWriteMode(CATALOG, "delete_set");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("label", label);
        m.executeUpdate(params);
    }

    /**
     * Remove a set
     * @param set the set to remove
     */
    public static void remove(RhnSet set) {
        removeByLabel(set.getUserId(), set.getLabel());
        RhnSetImpl simpl = (RhnSetImpl) set;
        simpl.getElements().clear();
        simpl.sync();
    }
}
