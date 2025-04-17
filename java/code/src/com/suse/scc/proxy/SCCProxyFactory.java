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
package com.suse.scc.proxy;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class SCCProxyFactory extends HibernateFactory {

    private static final Logger LOG = LogManager.getLogger(SCCProxyFactory.class);

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * Save a {@link SCCProxyRecord} object
     *
     * @param sccProxyRecord object to save
     */
    public void save(SCCProxyRecord sccProxyRecord) {
        saveObject(sccProxyRecord);
    }

    /**
     * Remove a {@link SCCProxyRecord} object
     *
     * @param sccProxyRecord object to remove
     */
    public void remove(SCCProxyRecord sccProxyRecord) {
        removeObject(sccProxyRecord);
    }

    /**
     * Lookup {@link SCCProxyRecord} object by its proxy generated id
     *
     * @param proxyId the proxy generated id
     * @return return {@link SCCProxyRecord} with the given proxy generated id or empty
     */
    public Optional<SCCProxyRecord> lookupByProxyId(long proxyId) {
        return getSession()
                .createQuery("FROM SCCProxyRecord WHERE proxyId = :proxyId", SCCProxyRecord.class)
                .setParameter("proxyId", proxyId)
                .uniqueResultOptional();
    }
}
