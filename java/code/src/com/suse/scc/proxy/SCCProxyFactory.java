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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
        sccProxyRecord.setModified(new Date());
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

    /**
     * Lookup {@link SCCProxyRecord} object by sccLogin and the status
     *
     * @param sccLoginIn the scc login
     * @param statusIn the status
     * @return return {@link SCCProxyRecord} or empty
     */
    public Optional<SCCProxyRecord> lookupBySccLoginAndStatus(String sccLoginIn, SccProxyStatus statusIn) {
        return getSession()
                .createQuery("FROM SCCProxyRecord WHERE sccLogin = :sccLogin AND status = :status",
                        SCCProxyRecord.class)
                .setParameter("sccLogin", sccLoginIn)
                .setParameter("status", statusIn)
                .uniqueResultOptional();
    }

    /**
     * Lookup {@link SCCProxyRecord} list of objects with given status
     *
     * @param statusIn the status
     * @return return list of {@link SCCProxyRecord} with the given status
     */
    public List<SCCProxyRecord> lookupByStatus(SccProxyStatus statusIn) {
        return getSession()
                .createQuery("FROM SCCProxyRecord WHERE status = :status", SCCProxyRecord.class)
                .setParameter("status", statusIn)
                .list();
    }

    /**
     * Lookup {@link SCCProxyRecord} list of objects with given status and with retry time not expired
     *
     * @param statusIn the status
     * @return return list of {@link SCCProxyRecord} with the given status
     */
    public List<SCCProxyRecord> lookupByStatusAndRetry(SccProxyStatus statusIn) {
        int regErrorExpireTime = Config.get().getInt(ConfigDefaults.REG_ERROR_EXPIRE_TIME, 168);
        Calendar retryTime = Calendar.getInstance();
        retryTime.add(Calendar.HOUR, -1 * regErrorExpireTime);

        return getSession()
                .createQuery("""
                            FROM SCCProxyRecord
                            WHERE status = :status
                            AND (sccRegistrationErrorTime IS NULL
                                 OR sccRegistrationErrorTime < :retryTime)
                        """, SCCProxyRecord.class)
                .setParameter("status", statusIn)
                .setParameter("retryTime", new Date(retryTime.getTimeInMillis()))
                .list();
    }

    /**
     * Returns registration items of systems which should be forwarded to SCC
     *
     * @return list of {@link SCCProxyRecord}
     */
    public List<SCCProxyRecord> findSystemsToForwardRegistration() {
        return lookupByStatusAndRetry(SccProxyStatus.SCC_CREATION_PENDING);
    }

    /**
     * Returns registration items of systems which should be de-registered from SCC
     *
     * @return list of {@link SCCProxyRecord}
     */
    public List<SCCProxyRecord> listDeregisterItems() {
        return lookupByStatusAndRetry(SccProxyStatus.SCC_REMOVAL_PENDING);
    }

    /**
     * Returns virtualization host items which should be forwarded to SCC
     *
     * @return list of {@link SCCProxyRecord}
     */
    public List<SCCProxyRecord> findVirtualizationHosts() {
        return lookupByStatusAndRetry(SccProxyStatus.SCC_VIRTHOST_PENDING);
    }
}
