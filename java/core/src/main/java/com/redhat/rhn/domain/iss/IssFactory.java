/*
 * Copyright (c) 2013--2014 Red Hat, Inc.
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

package com.redhat.rhn.domain.iss;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.org.Org;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * IssSlaveFactory - the singleton class used to fetch and store
 * com.redhat.rhn.domain.server.IssSlave objects from the database.
 */
public class IssFactory extends HibernateFactory {

    private static IssFactory singleton = new IssFactory();
    private static Logger log = LogManager.getLogger(IssFactory.class);

    private IssFactory() {
        super();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Lookup a IssSlave by its id
     * @param id the id to search for
     * @return the IssSlave found
     */
    public static IssSlave lookupSlaveById(Long id) {
        return singleton.lookupObjectByNamedQuery("IssSlave.findById", Map.of("id", id));
    }

    /**
     * Lookup a IssSlave by its name
     * @param inName the slave to search for
     * @return the IssSlave found
     */
    public static IssSlave lookupSlaveByName(String inName) {
        return singleton.lookupObjectByNamedQuery("IssSlave.findByName", Map.of("slave", inName));
    }

    /**
     * List all IssSlaves for this Master
     * @return list of all the slaves
     */
    public static List<IssSlave> listAllIssSlaves() {
        return singleton.listObjectsByNamedQuery("IssSlave.lookupAll", Map.of());
    }

    /**
     * Lookup a IssMaster by its id
     * @param id the id to search for
     * @return the IssMaster entry found
     */
    public static IssMaster lookupMasterById(Long id) {
        return singleton.lookupObjectByNamedQuery("IssMaster.findById", Map.of("id", id));
    }

    /**
     * Lookup a IssMaster by its name
     * @param label the label of the desired master
     * @return the IssMaster entry found
     */
    public static IssMaster lookupMasterByLabel(String label) {
        return singleton.lookupObjectByNamedQuery("IssMaster.findByLabel", Map.of("label", label));
    }

    /**
     * List all IssMaster entries for this Slave
     * @return list of all masters known to this slave
     */
    public static List<IssMaster> listAllMasters() {
        return singleton.listObjectsByNamedQuery("IssMaster.lookupAll", Map.of());
    }

    /**
     * Return current default master for this slave
     * @return master where master.isDefaultMaster() == true, null else
     */
    public static IssMaster getCurrentMaster() {
        return singleton.lookupObjectByNamedQuery("IssMaster.lookupDefaultMaster", Map.of());
    }

    /**
     * Unset whatever the 'current' master is, no matter who holds it currently
     */
    public static void unsetCurrentMaster() {
        IssMaster m = getCurrentMaster();
        if (m != null) {
            m.unsetAsDefault();
            save(m);
            HibernateFactory.getSession().flush();
        }
    }

    /**
     * Remove a given local-org from being mapped to any master-orgs
     * @param inOrg the local-org we want to unmap
     */
    public static void unmapLocalOrg(Org inOrg) {
        HibernateFactory.getSession().
            getNamedQuery("IssMasterOrg.unmapLocalOrg").
            setParameter("inOrg", inOrg).
            executeUpdate();
    }

    /**
     * Delete an entity.
     * @param entity to delete.
     */
    public static void delete(Object entity) {
        singleton.removeObject(entity);
    }

    /**
     * Insert or Update an entity.
     * @param entity to be stored in database.
     */
    public static void save(Object entity) {
        singleton.saveObject(entity);
    }

    /**
     * Remove an entity from the DB
     * @param entity to be removed from database.
     */
    public static void remove(Object entity) {
        singleton.removeObject(entity);
    }

}
