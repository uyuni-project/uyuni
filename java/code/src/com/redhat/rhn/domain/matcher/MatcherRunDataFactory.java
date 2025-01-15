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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;


/**
 * MatcherRunData hibernate factory.
 */
public class MatcherRunDataFactory extends HibernateFactory {

    private static final Logger LOG = LogManager.getLogger(MatcherRunDataFactory.class);

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
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<MatcherRunData> cq = cb.createQuery(MatcherRunData.class);
        Root<MatcherRunData> root = cq.from(MatcherRunData.class);
        cq.select(root);

        TypedQuery<MatcherRunData> query = getSession().createQuery(cq);

        try {
            return query.getSingleResult();
        }
        catch (NoResultException e) {
            return null;
        }
        catch (Exception e) {
            throw new RuntimeException("Error retrieving MatcherRunData", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
