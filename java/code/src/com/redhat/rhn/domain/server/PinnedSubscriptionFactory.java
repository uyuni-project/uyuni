package com.redhat.rhn.domain.server;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * todo
 */
public class PinnedSubscriptionFactory extends HibernateFactory {

    private static Logger log;
    private static PinnedSubscriptionFactory INSTANCE;

    private PinnedSubscriptionFactory() { }

    public static synchronized PinnedSubscriptionFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PinnedSubscriptionFactory();
        }
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        if (log == null) {
            log = Logger.getLogger(PinnedSubscriptionFactory.class);
        }
        return log;
    }

    public List<PinnedSubscription> listPinnedSubscriptions() {
        return getSession().createCriteria(PinnedSubscription.class).list();
    }

    public void save(PinnedSubscription subscription) {
        saveObject(subscription);
    }

    public void remove(PinnedSubscription subscription) {
        removeObject(subscription);
    }

    public void cleanStalePins() {
        // todo this should be probably rewritten as a named query in HQL
        for (PinnedSubscription pin : listPinnedSubscriptions()) {
            if (SCCCachingFactory
                    .lookupSubscriptionBySccId(pin.getSubscriptionId()) == null ||
                    ServerFactory.lookupById(pin.getSystemId()) == null) {
                remove(pin);
            }
        }
    }

}
