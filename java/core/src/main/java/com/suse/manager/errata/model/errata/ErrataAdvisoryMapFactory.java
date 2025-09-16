/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.errata.model.errata;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class ErrataAdvisoryMapFactory extends HibernateFactory {

    private static final Logger LOG = LogManager.getLogger(ErrataAdvisoryMapFactory.class);

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * Save a {@link ErrataAdvisoryMap} object
     *
     * @param advisoryMapEntryIn object to save
     */
    public void save(ErrataAdvisoryMap advisoryMapEntryIn) {
        saveObject(advisoryMapEntryIn);
    }

    /**
     * Remove a {@link ErrataAdvisoryMap} object
     *
     * @param advisoryMapEntryIn the object to remove
     */
    public void remove(ErrataAdvisoryMap advisoryMapEntryIn) {
        removeObject(advisoryMapEntryIn);
    }


    /**
     * Retrieves the ErrataAdvisoryMap item with the given advisory
     *
     * @param patchId the patch id to look for
     * @return the ErrataAdvisoryMap item instance, if present
     */
    public Optional<ErrataAdvisoryMap> lookupByPatchId(String patchId) {
        return getSession()
                .createQuery("FROM ErrataAdvisoryMap k WHERE k.patchId = :patchId", ErrataAdvisoryMap.class)
                .setParameter("patchId", patchId)
                .uniqueResultOptional();
    }

    /**
     * Count the existing table entries
     *
     * @return the current number of table entries
     */
    public long count() {
        return getSession()
                .createQuery("SELECT COUNT(*) FROM ErrataAdvisoryMap k", Long.class)
                .uniqueResult();
    }

    /**
     * Clear all repositories from the database.
     */
    public void clearErrataAdvisoryMap() {
        getSession().createQuery("DELETE FROM ErrataAdvisoryMap").executeUpdate();
    }
}


