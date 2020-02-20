package com.redhat.rhn.domain.recurringactions;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.log4j.Logger;

import java.util.List;

public class RecurringActionFactory extends HibernateFactory {

    private static final Logger LOG = Logger.getLogger(RecurringActionFactory.class);
    private static final RecurringActionFactory INSTANCE = new RecurringActionFactory();

    public static List<MinionRecurringAction> listMinionRecurringActions(Long id) { // TODO: Do we need user here?
       return getSession().createQuery("SELECT action FROM MinionRecurringAction action " +
               "WHERE action.minion.id = :mid")
               .setParameter("mid", id)
               .list();
    }

    public static void save(RecurringAction action) {
        INSTANCE.saveObject(action);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
