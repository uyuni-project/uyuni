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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class CloudRmtHostFactory extends HibernateFactory {

    private static Logger log = LogManager.getLogger(CloudRmtHostFactory.class);
    private static final CloudRmtHostFactory SINGLETON = new CloudRmtHostFactory();

    private CloudRmtHostFactory() {
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Create a new empty cloud rmt host object
     * @return cloud rmt host
     */
    public static CloudRmtHost createCloudRmtHost() {
        return new CloudRmtHost();
    }

    /**
     * Deletes the cloud rmt host object from the database
     * @param host cloud rmt host to be deleted
     */
    public static void deleteCloudRmtHost(CloudRmtHost host) {
        getSession().delete(host);
    }

    /**
     * save the cloud rmt host object to the database
     * @param host to be saved
     */
    public static void saveCloudRmtHost(CloudRmtHost host) {
        host.setModified(new Date());
        SINGLETON.saveObject(host);
    }

    /**
     * lookup for a cloud rmt host based on the hostname
     * @param hostname hostname to search for
     * @return an optional with the cloud rmt host, which can be empty
     */
    public static Optional<CloudRmtHost> lookupByHostname(String hostname) {
        if (hostname == null) {
            return Optional.empty();
        }
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<CloudRmtHost> select = builder.createQuery(CloudRmtHost.class);
        Root<CloudRmtHost> root = select.from(CloudRmtHost.class);
        select.where(builder.equal(root.get("host"), hostname));

        return getSession().createQuery(select).list()
                .stream().findFirst();
    }

    /**
     * Search for all existing cloud rmt hosts
     * @return list of cloud rmt host objects
     */
    public static List<CloudRmtHost> lookupCloudRmtHosts() {
        log.debug("Retrieving repositories from cache");
        return getSession().createQuery("FROM CloudRmtHost").list();
    }

    /**
     * Search for a list of cloud rmt host which should be used to update the host file
     * It will not contain duplicated host names
     * @return list of cloud rmt host objects
     */
    public static List<CloudRmtHost> lookupCloudRmtHostsToUpdate() {
        log.debug("Retrieving repositories from cache");
        return SINGLETON.listObjectsByNamedQuery("CloudRmtHost.listHostToUpdate",
                Collections.EMPTY_MAP);
    }
}
