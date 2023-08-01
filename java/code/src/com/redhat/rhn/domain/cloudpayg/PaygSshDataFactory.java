/*
 * Copyright (c) 2021 SUSE LLC
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

package com.redhat.rhn.domain.cloudpayg;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class PaygSshDataFactory extends HibernateFactory {

    private static Logger log = LogManager.getLogger(PaygSshDataFactory.class);
    private static final PaygSshDataFactory SINGLETON = new PaygSshDataFactory();

    private PaygSshDataFactory() {
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Create an empty payg ssh data object
     * @return payg ssh data object
     */
    public static PaygSshData createPaygSshData() {
        return new PaygSshData();
    }

    /**
     * Save the payg ssh data object to the database
     * @param sshData payg ssh data object
     */
    public static void savePaygSshData(PaygSshData sshData) {
        sshData.setModified(new Date());
        SINGLETON.saveObject(sshData);
    }

    /**
     * search for a payg ssh data object based on the hostname
     * @param hostname to use in the search
     * @return payg ssh data object optional
     */
    public static Optional<PaygSshData> lookupByHostname(String hostname) {
        if (hostname == null) {
            return Optional.empty();
        }
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<PaygSshData> select = builder.createQuery(PaygSshData.class);
        Root<PaygSshData> root = select.from(PaygSshData.class);
        select.where(builder.equal(root.get("host"), hostname));

        return getSession().createQuery(select).uniqueResultOptional();
    }

    /**
     * search for a payg ssh data object based on the database id
     * @param id database ID
     * @return payg ssh data object optional
     */
    public static Optional<PaygSshData> lookupById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<PaygSshData> select = builder.createQuery(PaygSshData.class);
        Root<PaygSshData> root = select.from(PaygSshData.class);
        select.where(builder.equal(root.get("id"), id));

        return getSession().createQuery(select).uniqueResultOptional();
    }

    /**
     * Search for all payg ssh data objects in the database
     * @return list of payg ssh daa objects
     */
    public static List<PaygSshData> lookupPaygSshData() {
        return getSession().createQuery("FROM PaygSshData").list();
    }

    /**
     * Deletes the payg ssh data object from the database
     * @param data payg ssh data object
     */
    public static void deletePaygSshData(PaygSshData data) {
        getSession().delete(data);
    }
}
