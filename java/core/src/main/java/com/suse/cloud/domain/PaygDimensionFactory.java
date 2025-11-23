/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.cloud.domain;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

/**
 * Factory to store and retrieve {@link PaygDimensionComputation}
 */
public class PaygDimensionFactory extends HibernateFactory {

    private static final Logger LOGGER = LogManager.getLogger(PaygDimensionFactory.class);

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    /**
     * Retrieves a result by its id
     * @param id the id of the computation
     * @return the computation result if present, or null
     */
    public PaygDimensionComputation lookupById(Long id) {
        return getSession().createQuery("FROM PaygDimensionComputation AS c WHERE c.id = :id",
                        PaygDimensionComputation.class)
                .setParameter("id", id)
                .uniqueResult();
    }

    /**
     * Persist the given instance in the database
     * @param computationResult the instance to persist
     */
    public void save(PaygDimensionComputation computationResult) {
        super.saveObject(computationResult);
    }

    /**
     * Retrieves the latest successful computation result.
     *
     * @return the latest valid result
     */
    public PaygDimensionComputation getLatestSuccessfulComputation() {
        Session session = getSession();
        return session.createQuery(
                        "FROM PaygDimensionComputation AS c WHERE c.success = true ORDER BY c.timestamp DESC",
                        PaygDimensionComputation.class)
                .setMaxResults(1)
                .uniqueResult();
    }
}
