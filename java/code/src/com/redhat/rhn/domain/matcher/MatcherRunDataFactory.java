/*
 * Copyright (c) 2016 SUSE LLC
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

package com.redhat.rhn.domain.matcher;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.log4j.Logger;

/**
 * MatcherRunData hibernate factory.
 */
public class MatcherRunDataFactory extends HibernateFactory {

    private static Logger log;

    /**
     * Wipes the old data, stores new MatcherRunData.
     * @param newData - new MatcherRunData
     */
    public static void updateData(MatcherRunData newData) {
        getSession().getNamedQuery("MatcherRunData.deleteAll").executeUpdate();
        getSession().save(newData);
    }

    /**
     * Gets a single MatcherRunData instance.
     *  - If there is no such instance, null is returned.
     *  - If there are more results, HibernateException is thrown.
     *
     * @return MatcherRunData instance
     */
    public static MatcherRunData getSingle() {
        return (MatcherRunData) getSession()
                .createCriteria(MatcherRunData.class)
                .uniqueResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        if (log == null) {
            log = Logger.getLogger(MatcherRunDataFactory.class);
        }
        return log;
    }
}
